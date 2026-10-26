package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Adds an existing account, found by email, to the current organization. {@code role} defaults to Member. */
public record MemberRequest(@NotBlank @Email String email, MemberRole role) {
}
