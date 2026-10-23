package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Body for creating or replacing a finding. The id is assigned by the server; resolved defaults to false. */
public record FindingRequest(
        @NotBlank String auditId,
        @NotBlank @Size(max = 255) String title,
        @NotNull Severity severity,
        @NotBlank @Size(max = 255) String owner,
        @NotNull LocalDate dueDate,
        Boolean resolved) {
}
