package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.MemberRole;
import java.util.List;

/**
 * Who is signed in, the organization their token works in with their role there, and every organization they can
 * switch to.
 */
public record SessionResponse(
        UserResponse user,
        OrganizationResponse organization,
        MemberRole role,
        List<OrganizationResponse> organizations) {
}
