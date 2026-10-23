package com.auditapp.audit.config;

import com.auditapp.audit.domain.AuditStatus;
import com.auditapp.audit.domain.Severity;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location")
                .maxAge(3600);
    }

    /** Lets query parameters use the same labels as JSON, for example ?status=In%20Progress. */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, AuditStatus.class, AuditStatus::from);
        registry.addConverter(String.class, Severity.class, Severity::from);
    }
}
