package com.auditapp.audit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Runs against the dev profile's seeded schema, signed in as the demo user in Acme Corp (the V1_1 sample data).
 * Each test gets a fresh context with its own uniquely named in-memory database, so data changes don't leak.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditApiTest {

    @Autowired
    MockMvc mvc;

    String token;

    @BeforeEach
    void signIn() throws Exception {
        token = TestLogins.token(mvc, TestLogins.DEMO);
    }

    ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(TestLogins.as(token, request));
    }

    @Test
    void listsSeededAuditsWithFrontEndLabels() throws Exception {
        perform(get("/audits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id").value("AUD-1001"))
                .andExpect(jsonPath("$[0].status").value("In Progress"))
                .andExpect(jsonPath("$[0].startDate").value("2026-10-01"));
    }

    @Test
    void filtersAuditsByTextAndStatus() throws Exception {
        perform(get("/audits").param("q", "arjun"))
                .andExpect(jsonPath("$", hasSize(2)));
        perform(get("/audits").param("q", "arjun").param("status", "Completed"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("AUD-1006"));
    }

    @Test
    void rejectsUnknownStatusFilter() throws Exception {
        perform(get("/audits").param("status", "Archived"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void returns404ForUnknownAudit() throws Exception {
        perform(get("/audits/AUD-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Audit AUD-9999 not found"));
    }

    @Test
    void listsFindingsForAudit() throws Exception {
        perform(get("/audits/AUD-1001/findings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].severity").value("High"));
    }

    @Test
    void createsUpdatesAndDeletesAnAudit() throws Exception {
        String body = """
                {"title":"Cash Handling Review","department":"Finance","auditor":"Priya Sharma",
                 "status":"Planned","startDate":"2026-12-01","endDate":"2026-12-15","progress":0,
                 "description":"Petty cash counts"}
                """;
        perform(post("/audits").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/audits/AUD-1009"))
                .andExpect(jsonPath("$.id").value("AUD-1009"));

        perform(put("/audits/AUD-1009").contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"Planned\"", "\"In Progress\"").replace("\"progress\":0", "\"progress\":20")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("In Progress"))
                .andExpect(jsonPath("$.progress").value(20));

        perform(delete("/audits/AUD-1009")).andExpect(status().isNoContent());
        perform(get("/audits/AUD-1009")).andExpect(status().isNotFound());
    }

    @Test
    void validatesAuditBody() throws Exception {
        String body = """
                {"title":"","department":"Finance","auditor":"X","status":"Planned",
                 "startDate":"2026-12-15","endDate":"2026-12-01","progress":150}
                """;
        perform(post("/audits").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.progress").exists())
                .andExpect(jsonPath("$.errors.dateRangeValid").value("endDate must not be before startDate"));
    }

    @Test
    void deletingAnAuditRemovesItsFindings() throws Exception {
        perform(delete("/audits/AUD-1001")).andExpect(status().isNoContent());
        perform(get("/findings/F-201")).andExpect(status().isNotFound());
    }

    @Test
    void createsAndResolvesAFinding() throws Exception {
        String body = """
                {"auditId":"AUD-1003","title":"Vendor KYC missing","severity":"Critical",
                 "owner":"Procurement","dueDate":"2026-12-01"}
                """;
        perform(post("/findings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("F-210"))
                .andExpect(jsonPath("$.resolved").value(false));

        perform(patch("/findings/F-210").contentType(MediaType.APPLICATION_JSON).content("{\"resolved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true));
    }

    @Test
    void rejectsFindingForUnknownAudit() throws Exception {
        String body = """
                {"auditId":"AUD-9999","title":"x","severity":"Low","owner":"x","dueDate":"2026-12-01"}
                """;
        perform(post("/findings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtersFindings() throws Exception {
        perform(get("/findings").param("resolved", "false"))
                .andExpect(jsonPath("$", hasSize(4)));
        perform(get("/findings").param("severity", "High").param("resolved", "false"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void dashboardMatchesFrontEndMetrics() throws Exception {
        perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAudits").value(6))
                .andExpect(jsonPath("$.activeAudits").value(3))
                .andExpect(jsonPath("$.completedAudits").value(2))
                .andExpect(jsonPath("$.openFindings").value(4))
                .andExpect(jsonPath("$.openHighRiskFindings").value(3))
                .andExpect(jsonPath("$.auditsByStatus['In Progress']").value(2))
                .andExpect(jsonPath("$.openFindingsBySeverity.Critical").value(1));
    }

    @Test
    void healthIsUp() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
}
