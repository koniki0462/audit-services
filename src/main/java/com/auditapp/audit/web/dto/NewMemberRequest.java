package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.Gender;
import com.auditapp.audit.domain.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Creates an account and adds it to the current organization. The admin chooses the initial password and gives
 * it to the new member. {@code role} defaults to Member. BCrypt only uses the first 72 bytes of a password.
 */
public record NewMemberRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull Gender gender,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^\\+?[0-9 ()\\-]{5,30}$",
                message = "must be a phone number: digits, spaces, ( ) - and an optional leading +") String phoneNumber,
        @NotBlank @Size(max = 1000) String address,
        @NotBlank @Size(min = 8, max = 72) String password,
        MemberRole role) {
}
