package com.auditapp.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Signs in as the dev seed's users (password "password123", see db/seed/V2_1). */
final class TestLogins {

    static final String ACME = "00000000-0000-0000-0000-000000000001";
    static final String GLOBEX = "00000000-0000-0000-0000-000000000002";
    /** Member of Acme Corp and Globex Ltd; logs in to Acme by default. */
    static final String DEMO = "demo@auditapp.dev";
    /** Member of Globex Ltd only. */
    static final String GLOBEX_OWNER = "owner@globex.dev";

    private TestLogins() {
    }

    static String token(MockMvc mvc, String email) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    static MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
