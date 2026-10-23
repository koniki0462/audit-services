package com.auditapp.audit.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Adds an existing account, found by email, to the current organization. */
public record MemberRequest(@NotBlank @Email String email) {
}
