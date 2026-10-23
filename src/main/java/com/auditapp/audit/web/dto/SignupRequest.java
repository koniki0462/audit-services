package com.auditapp.audit.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Creates a user and their first organization (add its other details later with PUT /organization). BCrypt only uses the first 72 bytes of a password. */
public record SignupRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 255) String organizationLegalName) {
}
