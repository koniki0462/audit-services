package com.auditapp.audit.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * The signed-in user and the organization their token works in. Services scope every query with
 * {@link #organizationId()}. Only valid inside an authenticated request.
 */
@Component
public class CurrentUser {

    public String userId() {
        return token().getSubject();
    }

    public String organizationId() {
        return token().getClaimAsString(TokenService.ORGANIZATION_CLAIM);
    }

    private static Jwt token() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken();
        }
        throw new IllegalStateException("No authenticated user in this request");
    }
}
