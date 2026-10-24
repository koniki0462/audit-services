package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.Role;
import java.time.Instant;

public record RoleResponse(
        long id,
        String name,
        String description,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt) {

    public static RoleResponse from(Role r) {
        return new RoleResponse(r.getId(), r.getName(), r.getDescription(), r.isActive(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
