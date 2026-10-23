package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.AuditStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Body for creating or replacing an audit. The id is assigned by the server. */
public record AuditRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String department,
        @NotBlank @Size(max = 255) String auditor,
        @NotNull AuditStatus status,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @Min(0) @Max(100) Integer progress,
        @Size(max = 2000) String description) {

    @JsonIgnore
    @AssertTrue(message = "endDate must not be before startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
