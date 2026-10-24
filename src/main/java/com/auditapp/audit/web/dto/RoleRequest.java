package com.auditapp.audit.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code isActive} defaults to true when left out. */
public record RoleRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 2000) String description,
        Boolean isActive) {
}
