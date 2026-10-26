package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.Gender;
import com.auditapp.audit.domain.MemberRole;
import com.auditapp.audit.domain.UserAccount;

/** A member of the current organization, with their role there. Profile fields are null for accounts from sign-up. */
public record MemberResponse(
        String id,
        String email,
        String name,
        String firstName,
        String lastName,
        Gender gender,
        String phoneNumber,
        String address,
        MemberRole role) {

    public static MemberResponse from(UserAccount u, MemberRole role) {
        return new MemberResponse(u.getId(), u.getEmail(), u.getName(), u.getFirstName(), u.getLastName(),
                u.getGender(), u.getPhoneNumber(), u.getAddress(), role);
    }
}
