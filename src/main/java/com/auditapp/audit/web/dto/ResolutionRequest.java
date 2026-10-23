package com.auditapp.audit.web.dto;

import jakarta.validation.constraints.NotNull;

/** Body for PATCH /findings/{id}: mark a finding resolved or reopen it. */
public record ResolutionRequest(@NotNull Boolean resolved) {
}
