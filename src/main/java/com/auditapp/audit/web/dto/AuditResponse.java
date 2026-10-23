package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.Audit;
import com.auditapp.audit.domain.AuditStatus;
import java.time.Instant;
import java.time.LocalDate;

/** Matches the front end's Audit type, plus timestamps. */
public record AuditResponse(
        String id,
        String title,
        String department,
        String auditor,
        AuditStatus status,
        LocalDate startDate,
        LocalDate endDate,
        int progress,
        String description,
        Instant createdAt,
        Instant updatedAt) {

    public static AuditResponse from(Audit a) {
        return new AuditResponse(a.getId(), a.getTitle(), a.getDepartment(), a.getAuditor(), a.getStatus(),
                a.getStartDate(), a.getEndDate(), a.getProgress(), a.getDescription(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
