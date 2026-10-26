package com.auditapp.audit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** Swagger UI and the OpenAPI document are public in dev and describe auth and errors correctly. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired
    MockMvc mvc;

    @Test
    void swaggerUiIsPublic() throws Exception {
        mvc.perform(get("/docs")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
    }

    @Test
    void describesTheApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("AuditApp API"))
                .andExpect(jsonPath("$.info.version").isNotEmpty())
                .andExpect(jsonPath("$.info.description", containsString("POST /auth/login")))
                .andExpect(jsonPath("$.security[0]", hasKey("bearer")))
                .andExpect(jsonPath("$.components.securitySchemes.bearer.scheme").value("bearer"))
                .andExpect(jsonPath("$.tags[*].name", not(empty())));
    }

    @Test
    void loginAndSignupNeedNoToken() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/auth/login'].post.security", empty()))
                .andExpect(jsonPath("$.paths['/auth/signup'].post.security", empty()))
                .andExpect(jsonPath("$.paths['/auth/login'].post.responses", not(hasKey("401"))))
                .andExpect(jsonPath("$.paths['/audits'].get.responses", hasKey("401")));
    }

    @Test
    void documentsErrorResponses() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/audits/{id}'].get.responses", hasKey("404")))
                .andExpect(jsonPath("$.paths['/audits'].post.responses", hasKey("400")))
                .andExpect(jsonPath("$.paths['/audits/{id}'].put.responses", hasKey("409")))
                .andExpect(jsonPath("$.paths['/audits'].get.responses", not(hasKey("409"))))
                .andExpect(jsonPath("$.paths['/audits/{id}'].get.responses['404'].content['application/problem+json']").exists())
                .andExpect(jsonPath("$.components.schemas.ApiProblem.properties.detail").exists());
    }

    @Test
    void everyOperationHasASummary() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths.*.*[?(!@.summary)]", empty()));
    }
}
