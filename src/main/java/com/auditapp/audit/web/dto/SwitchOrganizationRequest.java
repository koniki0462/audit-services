package com.auditapp.audit.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SwitchOrganizationRequest(@NotBlank String organizationId) {
}
