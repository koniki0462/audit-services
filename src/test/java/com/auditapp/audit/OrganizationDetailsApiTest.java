package com.auditapp.audit;

import static com.auditapp.audit.TestLogins.DEMO;
import static com.auditapp.audit.TestLogins.GLOBEX_OWNER;
import static com.auditapp.audit.TestLogins.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** GET/PUT /organization: the current organization's core details. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrganizationDetailsApiTest {

    private static final String FULL_DETAILS = """
            {"legalName":"Acme Corporation Pvt Ltd","tradeName":"Acme","entityType":"Corporation",
             "industryCode":"332510","businessPurpose":"Tools and hardware.","dateOfIncorporation":"2009-04-01",
             "contactEmail":"Compliance@Acme.test","contactNumber":"+91 80 4000 1000",
             "address":"12 MG Road, Bengaluru","fiscalYearEndMonth":3}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void returnsTheCurrentOrganizationsDetails() throws Exception {
        mvc.perform(as(TestLogins.token(mvc, DEMO), get("/organization")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestLogins.ACME))
                .andExpect(jsonPath("$.legalName").value("Acme Corp"))
                .andExpect(jsonPath("$.tradeName").value("Acme"))
                .andExpect(jsonPath("$.entityType").value("Corporation"))
                .andExpect(jsonPath("$.dateOfIncorporation").value("2009-04-01"))
                .andExpect(jsonPath("$.fiscalYearEndMonth").value(3))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void replacesDetailsButNotStatusOrIsActive() throws Exception {
        String token = TestLogins.token(mvc, DEMO);
        mvc.perform(as(token, json(put("/organization"),
                        FULL_DETAILS.replace("\"fiscalYearEndMonth\":3", "\"fiscalYearEndMonth\":3,\"status\":\"Dissolved\",\"isActive\":false"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legalName").value("Acme Corporation Pvt Ltd"))
                .andExpect(jsonPath("$.contactEmail").value("compliance@acme.test"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.isActive").value(true));

        // Optional fields left out or blank are cleared.
        mvc.perform(as(token, json(put("/organization"), "{\"legalName\":\"Acme Corp\",\"tradeName\":\"  \"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeName").doesNotExist())
                .andExpect(jsonPath("$.entityType").doesNotExist())
                .andExpect(jsonPath("$.fiscalYearEndMonth").doesNotExist());
        mvc.perform(as(token, get("/auth/me"))).andExpect(jsonPath("$.organization.legalName").value("Acme Corp"));
    }

    @Test
    void validatesDetails() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        mvc.perform(as(TestLogins.token(mvc, DEMO), json(put("/organization"), """
                        {"legalName":"","entityType":"Cooperative","fiscalYearEndMonth":13}
                        """)))
                .andExpect(status().isBadRequest());
        mvc.perform(as(TestLogins.token(mvc, DEMO), json(put("/organization"), """
                        {"legalName":"","contactEmail":"nope","contactNumber":"call me","dateOfIncorporation":"%s",
                         "fiscalYearEndMonth":13}
                        """.formatted(tomorrow))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.legalName").exists())
                .andExpect(jsonPath("$.errors.contactEmail").exists())
                .andExpect(jsonPath("$.errors.contactNumber").exists())
                .andExpect(jsonPath("$.errors.dateOfIncorporation").exists())
                .andExpect(jsonPath("$.errors.fiscalYearEndMonth").exists());
    }

    @Test
    void legalNamesAreUniqueIgnoringCase() throws Exception {
        String globex = TestLogins.token(mvc, GLOBEX_OWNER);
        mvc.perform(as(globex, json(put("/organization"), "{\"legalName\":\"ACME CORP\"}")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An organization with this legal name already exists."));
        mvc.perform(as(globex, json(post("/organizations"), "{\"legalName\":\"acme corp\"}")))
                .andExpect(status().isConflict());
        mvc.perform(json(post("/auth/signup"), """
                        {"name":"X","email":"x@new.test","password":"long-enough","organizationLegalName":"Globex Ltd"}
                        """))
                .andExpect(status().isConflict());
        // Saving your own name unchanged is fine.
        mvc.perform(as(globex, json(put("/organization"), "{\"legalName\":\"Globex Ltd\"}"))).andExpect(status().isOk());
    }

    @Test
    void newOrganizationsAreActiveWithTheGivenDetails() throws Exception {
        mvc.perform(as(TestLogins.token(mvc, DEMO), json(post("/organizations"),
                        FULL_DETAILS.replace("Acme Corporation Pvt Ltd", "Initech Pvt Ltd"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organization.legalName").value("Initech Pvt Ltd"))
                .andExpect(jsonPath("$.organization.entityType").value("Corporation"))
                .andExpect(jsonPath("$.organization.status").value("Active"))
                .andExpect(jsonPath("$.organization.isActive").value(true));
    }

    @Test
    void migrationKeptExistingOrganizationsAndRenamedTheKey() {
        Integer rows = jdbc.queryForObject("""
                select count(*) from organizations
                where organization_id in ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002')
                  and status = 'ACTIVE' and is_active""", Integer.class);
        assertThat(rows).isEqualTo(2);
    }
}
