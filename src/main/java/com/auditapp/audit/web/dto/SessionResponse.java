package com.auditapp.audit.web.dto;

import java.util.List;

/** Who is signed in, the organization their token works in, and every organization they can switch to. */
public record SessionResponse(
        UserResponse user,
        OrganizationResponse organization,
        List<OrganizationResponse> organizations) {
}
