package com.auditapp.audit.web.dto;

import jakarta.validation.constraints.NotBlank;

/** organizationId is optional; without it the token is for the user's first organization by name. */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password,
        String organizationId) {
}
