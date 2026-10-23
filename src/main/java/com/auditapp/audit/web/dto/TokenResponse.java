package com.auditapp.audit.web.dto;

import java.time.Instant;
import java.util.List;

/** An access token (send it as "Authorization: Bearer ...") plus the session it opens. */
public record TokenResponse(
        String token,
        Instant expiresAt,
        UserResponse user,
        OrganizationResponse organization,
        List<OrganizationResponse> organizations) {
}
