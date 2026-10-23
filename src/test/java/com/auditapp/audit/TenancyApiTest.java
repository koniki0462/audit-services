package com.auditapp.audit;

import static com.auditapp.audit.TestLogins.ACME;
import static com.auditapp.audit.TestLogins.DEMO;
import static com.auditapp.audit.TestLogins.GLOBEX;
import static com.auditapp.audit.TestLogins.GLOBEX_OWNER;
import static com.auditapp.audit.TestLogins.as;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Sign-up, login, organization switching and membership, and isolation between organizations. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TenancyApiTest {

    @Autowired
    MockMvc mvc;

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private String signup(String name, String email, String organization) throws Exception {
        String body = mvc.perform(json(post("/auth/signup"), """
                        {"name":"%s","email":"%s","password":"s3cret-pass","organizationLegalName":"%s"}
                        """.formatted(name, email, organization)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void requiresATokenForData() throws Exception {
        mvc.perform(get("/audits"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rejectsATamperedToken() throws Exception {
        String token = TestLogins.token(mvc, DEMO);
        String tampered = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");
        mvc.perform(as(tampered, get("/audits"))).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsWrongPasswordAndUnknownEmailAlike() throws Exception {
        mvc.perform(json(post("/auth/login"), "{\"email\":\"demo@auditapp.dev\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password."));
        mvc.perform(json(post("/auth/login"), "{\"email\":\"nobody@auditapp.dev\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password."));
    }

    @Test
    void loginReturnsTheSessionAndAcceptsAnyEmailCase() throws Exception {
        mvc.perform(json(post("/auth/login"), "{\"email\":\" Demo@AuditApp.dev \",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("demo@auditapp.dev"))
                .andExpect(jsonPath("$.organization.legalName").value("Acme Corp"))
                .andExpect(jsonPath("$.organizations", hasSize(2)));
    }

    @Test
    void organizationsSeeOnlyTheirOwnData() throws Exception {
        String globex = TestLogins.token(mvc, GLOBEX_OWNER);

        mvc.perform(as(globex, get("/audits")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("AUD-1007"));
        mvc.perform(as(globex, get("/findings"))).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(as(globex, get("/audits").param("q", "arjun"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(as(globex, get("/dashboard/summary")))
                .andExpect(jsonPath("$.totalAudits").value(2))
                .andExpect(jsonPath("$.openFindings").value(2));

        // Acme's records look like they don't exist, for reads and writes alike.
        mvc.perform(as(globex, get("/audits/AUD-1001"))).andExpect(status().isNotFound());
        mvc.perform(as(globex, get("/audits/AUD-1001/findings"))).andExpect(status().isNotFound());
        mvc.perform(as(globex, get("/findings/F-201"))).andExpect(status().isNotFound());
        mvc.perform(as(globex, get("/findings").param("auditId", "AUD-1001"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(as(globex, json(patch("/findings/F-201"), "{\"resolved\":true}"))).andExpect(status().isNotFound());
        mvc.perform(as(globex, delete("/findings/F-201"))).andExpect(status().isNotFound());
        mvc.perform(as(globex, delete("/audits/AUD-1001"))).andExpect(status().isNotFound());
        mvc.perform(as(globex, json(put("/audits/AUD-1001"), """
                        {"title":"Taken over","department":"x","auditor":"x","status":"Planned",
                         "startDate":"2026-12-01","endDate":"2026-12-15","progress":0}
                        """)))
                .andExpect(status().isNotFound());
        mvc.perform(as(globex, json(post("/findings"), """
                        {"auditId":"AUD-1001","title":"x","severity":"Low","owner":"x","dueDate":"2026-12-01"}
                        """)))
                .andExpect(status().isNotFound());
        // Moving a Globex finding onto an Acme audit is refused too.
        mvc.perform(as(globex, json(put("/findings/F-208"), """
                        {"auditId":"AUD-1001","title":"x","severity":"Low","owner":"x","dueDate":"2026-12-01"}
                        """)))
                .andExpect(status().isNotFound());

        String acme = TestLogins.token(mvc, DEMO);
        mvc.perform(as(acme, get("/audits/AUD-1001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Quarterly Financial Controls"));
        mvc.perform(as(acme, get("/findings/F-201"))).andExpect(jsonPath("$.resolved").value(false));
    }

    @Test
    void newRecordsBelongToTheCallersOrganization() throws Exception {
        String globex = TestLogins.token(mvc, GLOBEX_OWNER);
        mvc.perform(as(globex, json(post("/audits"), """
                        {"title":"Globex Treasury","department":"Finance","auditor":"Hank","status":"Planned",
                         "startDate":"2026-12-01","endDate":"2026-12-15","progress":0}
                        """)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("AUD-1009"));

        String acme = TestLogins.token(mvc, DEMO);
        mvc.perform(as(acme, get("/audits/AUD-1009"))).andExpect(status().isNotFound());
        mvc.perform(as(acme, get("/audits"))).andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void switchesBetweenOrganizationsTheUserBelongsTo() throws Exception {
        String acme = TestLogins.token(mvc, DEMO);
        mvc.perform(as(acme, get("/auth/me")))
                .andExpect(jsonPath("$.organization.id").value(ACME))
                .andExpect(jsonPath("$.organizations[1].legalName").value("Globex Ltd"));

        String body = mvc.perform(as(acme, json(post("/auth/switch"), "{\"organizationId\":\"" + GLOBEX + "\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organization.legalName").value("Globex Ltd"))
                .andReturn().getResponse().getContentAsString();
        String globex = JsonPath.read(body, "$.token");
        mvc.perform(as(globex, get("/audits"))).andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(json(post("/auth/login"), """
                        {"email":"demo@auditapp.dev","password":"password123","organizationId":"%s"}
                        """.formatted(GLOBEX)))
                .andExpect(jsonPath("$.organization.id").value(GLOBEX));
    }

    @Test
    void cannotSwitchIntoAnotherCompanysOrganization() throws Exception {
        String globex = TestLogins.token(mvc, GLOBEX_OWNER);
        mvc.perform(as(globex, json(post("/auth/switch"), "{\"organizationId\":\"" + ACME + "\"}")))
                .andExpect(status().isNotFound());
        mvc.perform(json(post("/auth/login"), """
                        {"email":"owner@globex.dev","password":"password123","organizationId":"%s"}
                        """.formatted(ACME)))
                .andExpect(status().isNotFound());
    }

    @Test
    void signupCreatesAnEmptyOrganization() throws Exception {
        String token = signup("Lisa Simpson", "Lisa@Springfield.test", "Springfield Elementary");
        mvc.perform(as(token, get("/auth/me")))
                .andExpect(jsonPath("$.user.email").value("lisa@springfield.test"))
                .andExpect(jsonPath("$.organization.legalName").value("Springfield Elementary"))
                .andExpect(jsonPath("$.organizations", hasSize(1)));
        mvc.perform(as(token, get("/audits"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(as(token, get("/dashboard/summary"))).andExpect(jsonPath("$.totalAudits").value(0));
    }

    @Test
    void signupRejectsDuplicateEmailAndBadInput() throws Exception {
        mvc.perform(json(post("/auth/signup"), """
                        {"name":"Demo","email":"DEMO@auditapp.dev","password":"long-enough","organizationLegalName":"X"}
                        """))
                .andExpect(status().isConflict());
        mvc.perform(json(post("/auth/signup"), """
                        {"name":"","email":"not-an-email","password":"short","organizationLegalName":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.organizationLegalName").exists());
    }

    @Test
    void membersCanBeAddedAndRemoved() throws Exception {
        String lisa = signup("Lisa Simpson", "lisa@springfield.test", "Springfield Elementary");
        String globex = TestLogins.token(mvc, GLOBEX_OWNER);

        mvc.perform(as(globex, json(post("/organization/members"), "{\"email\":\"lisa@springfield.test\"}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lisa Simpson"));
        mvc.perform(as(globex, json(post("/organization/members"), "{\"email\":\"lisa@springfield.test\"}")))
                .andExpect(status().isConflict());
        mvc.perform(as(globex, json(post("/organization/members"), "{\"email\":\"nobody@springfield.test\"}")))
                .andExpect(status().isNotFound());
        mvc.perform(as(globex, get("/organization/members"))).andExpect(jsonPath("$", hasSize(3)));

        String body = mvc.perform(as(lisa, json(post("/auth/switch"), "{\"organizationId\":\"" + GLOBEX + "\"}")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String lisaInGlobex = JsonPath.read(body, "$.token");
        String lisaId = JsonPath.read(body, "$.user.id");
        mvc.perform(as(lisaInGlobex, get("/audits"))).andExpect(jsonPath("$", hasSize(2)));

        // Removal takes effect immediately, even for a token issued before it.
        mvc.perform(as(globex, delete("/organization/members/" + lisaId))).andExpect(status().isNoContent());
        mvc.perform(as(lisaInGlobex, get("/audits"))).andExpect(status().isUnauthorized());
        mvc.perform(as(lisa, get("/audits"))).andExpect(status().isOk());
    }

    @Test
    void anOrganizationKeepsAtLeastOneMember() throws Exception {
        String lisa = signup("Lisa Simpson", "lisa@springfield.test", "Springfield Elementary");
        String lisaId = JsonPath.read(mvc.perform(as(lisa, get("/auth/me"))).andReturn().getResponse().getContentAsString(),
                "$.user.id");
        mvc.perform(as(lisa, delete("/organization/members/" + lisaId))).andExpect(status().isConflict());
    }

    @Test
    void createsAnotherOrganization() throws Exception {
        String acme = TestLogins.token(mvc, DEMO);
        String body = mvc.perform(as(acme, json(post("/organizations"), "{\"legalName\":\"Initech\"}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organization.legalName").value("Initech"))
                .andExpect(jsonPath("$.organizations", hasSize(3)))
                .andReturn().getResponse().getContentAsString();
        String initech = JsonPath.read(body, "$.token");
        mvc.perform(as(initech, get("/audits"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(as(initech, get("/organization/members"))).andExpect(jsonPath("$", hasSize(1)));
    }
}
