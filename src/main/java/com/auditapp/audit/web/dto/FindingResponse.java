package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import java.time.Instant;
import java.time.LocalDate;

/** Matches the front end's Finding type, plus timestamps. */
public record FindingResponse(
        String id,
        String auditId,
        String title,
        Severity severity,
        String owner,
        LocalDate dueDate,
        boolean resolved,
        Instant createdAt,
        Instant updatedAt) {

    public static FindingResponse from(Finding f) {
        return new FindingResponse(f.getId(), f.getAuditId(), f.getTitle(), f.getSeverity(), f.getOwner(),
                f.getDueDate(), f.isResolved(), f.getCreatedAt(), f.getUpdatedAt());
    }
}
