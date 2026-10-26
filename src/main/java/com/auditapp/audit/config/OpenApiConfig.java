package com.auditapp.audit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The OpenAPI document behind Swagger UI (/api/docs, disabled in prod). Every operation needs the bearer token
 * unless it is annotated with an empty {@code @SecurityRequirements}, like login and sign-up.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearer";
    private static final String PROBLEM = "ApiProblem";

    private static final String DESCRIPTION = """
            REST API for AuditApp: organizations, their members, audits, findings and dashboard metrics.

            **Trying it out**
            1. Call `POST /auth/login` (dev: `demo@auditapp.dev` / `password123`) or `POST /auth/signup`.
            2. Copy `token` from the response, click **Authorize** and paste it.
            3. Every other call now works in that token's organization. `POST /auth/switch` gets a token for
               another organization you belong to.

            Records of other organizations answer 404. Errors are RFC 9457 `application/problem+json`; validation
            errors add an `errors` map from field to message. Enum values accept the label (`In Progress`) or the
            constant (`IN_PROGRESS`); dates are `YYYY-MM-DD`.""";

    @Bean
    OpenAPI auditAppOpenApi(ObjectProvider<BuildProperties> build) {
        BuildProperties props = build.getIfAvailable();
        return new OpenAPI()
                .info(new Info()
                        .title("AuditApp API")
                        .version(props != null ? props.getVersion() : "dev")
                        .description(DESCRIPTION))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components()
                        .addSecuritySchemes(BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .description("Token from POST /auth/login, /auth/signup or /auth/switch")))
                .tags(List.of(
                        new Tag().name("Auth").description("Sign up, sign in, and switch organization"),
                        new Tag().name("Organizations").description("The current organization's details and members"),
                        new Tag().name("Audits").description("Audits of the current organization"),
                        new Tag().name("Findings").description("Findings raised in the current organization's audits"),
                        new Tag().name("Dashboard").description("Metrics for the dashboard"),
                        new Tag().name("Roles").description("The role catalogue, shared by all organizations")));
    }

    /**
     * Documents the error responses every operation can return, based on its shape. The problem schema is added
     * here rather than on the OpenAPI bean, because springdoc rebuilds the component schemas from the controllers.
     */
    @Bean
    OpenApiCustomizer errorResponses() {
        return openApi -> {
            openApi.getComponents().addSchemas(PROBLEM, problemSchema());
            openApi.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, op) -> {
                ApiResponses responses = op.getResponses();
                if (op.getRequestBody() != null) {
                    add(responses, "400", "Invalid body; `errors` maps each field to its message");
                }
                if (requiresToken(op)) {
                    add(responses, "401", "Missing, expired or revoked token");
                }
                if (path.contains("{")) {
                    add(responses, "404", "Not found in the current organization");
                }
                if (method != PathItem.HttpMethod.GET) {
                    add(responses, "409", "Conflicts with existing data or a concurrent change");
                }
            }));
        };
    }

    private static boolean requiresToken(Operation op) {
        // null means the document-wide requirement (bearer) applies; an empty list means public.
        return op.getSecurity() == null || !op.getSecurity().isEmpty();
    }

    private static void add(ApiResponses responses, String code, String description) {
        if (!responses.containsKey(code)) {
            responses.addApiResponse(code, new ApiResponse().description(description).content(new Content()
                    .addMediaType("application/problem+json",
                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + PROBLEM)))));
        }
    }

    private static Schema<?> problemSchema() {
        return new ObjectSchema()
                .description("RFC 9457 problem details")
                .properties(new LinkedHashMap<>(Map.of(
                        "type", new StringSchema().example("about:blank"),
                        "title", new StringSchema().example("Not Found"),
                        "status", new IntegerSchema().example(404),
                        "detail", new StringSchema().example("Audit AUD-9999 not found"),
                        "instance", new StringSchema().example("/api/audits/AUD-9999"),
                        "errors", new MapSchema().additionalProperties(new StringSchema())
                                .description("Validation errors only: field name to message"))));
    }
}
