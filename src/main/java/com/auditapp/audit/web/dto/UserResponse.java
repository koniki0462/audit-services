package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.UserAccount;

public record UserResponse(String id, String email, String name, boolean isApplicationAdmin) {

    public static UserResponse from(UserAccount u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getName(), u.isApplicationAdmin());
    }
}
