package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.MemberRole;
import java.time.Instant;
import java.util.List;

/** An access token (send it as "Authorization: Bearer ...") plus the session it opens. */
public record TokenResponse(
        String token,
        Instant expiresAt,
        UserResponse user,
        OrganizationResponse organization,
        MemberRole role,
        List<OrganizationResponse> organizations) {
}
