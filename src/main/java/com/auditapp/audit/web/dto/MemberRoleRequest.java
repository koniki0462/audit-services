package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.MemberRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleRequest(@NotNull MemberRole role) {
}
