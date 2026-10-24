package com.auditapp.audit;

import static com.auditapp.audit.TestLogins.DEMO;
import static com.auditapp.audit.TestLogins.as;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** /roles CRUD against the four roles created by V2. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoleApiTest {

    @Autowired
    MockMvc mvc;

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void requiresAToken() throws Exception {
        mvc.perform(get("/roles")).andExpect(status().isUnauthorized());
    }

    @Test
    void listsTheDefaultRolesByName() throws Exception {
        String token = TestLogins.token(mvc, DEMO);
        mvc.perform(as(token, get("/roles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].name").value("ADMIN"))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[3].name").value("USER"));
        mvc.perform(as(token, get("/roles?active=false"))).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createsReadsUpdatesAndDeletes() throws Exception {
        String token = TestLogins.token(mvc, DEMO);
        mvc.perform(as(token, json(post("/roles"), "{\"name\":\" auditor \",\"description\":\"Runs audits\"}")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("AUDITOR"))
                .andExpect(jsonPath("$.isActive").value(true));
        // Ids come from an identity column; the new role is the fifth.
        mvc.perform(as(token, get("/roles/5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Runs audits"));
        mvc.perform(as(token, json(put("/roles/5"), "{\"name\":\"Reviewer\",\"description\":\"  \",\"isActive\":false}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("REVIEWER"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.isActive").value(false));
        mvc.perform(as(token, get("/roles?active=false"))).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(as(token, delete("/roles/5"))).andExpect(status().isNoContent());
        mvc.perform(as(token, get("/roles/5"))).andExpect(status().isNotFound());
        mvc.perform(as(token, delete("/roles/5"))).andExpect(status().isNotFound());
        mvc.perform(as(token, json(put("/roles/5"), "{\"name\":\"X\"}"))).andExpect(status().isNotFound());
    }

    @Test
    void namesAreUniqueIgnoringCase() throws Exception {
        String token = TestLogins.token(mvc, DEMO);
        mvc.perform(as(token, json(post("/roles"), "{\"name\":\"admin\"}"))).andExpect(status().isConflict());
        mvc.perform(as(token, json(put("/roles/2"), "{\"name\":\"Admin\"}"))).andExpect(status().isConflict());
        // Saving a role under its own name is fine.
        mvc.perform(as(token, json(put("/roles/2"), "{\"name\":\"manager\"}"))).andExpect(status().isOk());
    }

    @Test
    void validatesTheRequest() throws Exception {
        String token = TestLogins.token(mvc, DEMO);
        mvc.perform(as(token, json(post("/roles"), "{\"name\":\"  \"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
        mvc.perform(as(token, json(post("/roles"), "{\"name\":\"" + "A".repeat(51) + "\"}")))
                .andExpect(status().isBadRequest());
    }
}
