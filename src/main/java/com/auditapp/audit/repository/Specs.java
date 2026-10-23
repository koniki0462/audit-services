package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Audit;
import com.auditapp.audit.domain.AuditStatus;
import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import java.util.Locale;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;

/** Filters for the list endpoints. The organization is required; for the other arguments null means no filter. */
public final class Specs {

    private Specs() {
    }

    /** Matches like the front end's filterAudits: case-insensitive substring of id, title, department or auditor. */
    public static Specification<Audit> auditMatches(String organizationId, String query, AuditStatus status) {
        Specification<Audit> spec = inOrganization(organizationId);
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("id")), like),
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("department")), like),
                    cb.like(cb.lower(root.get("auditor")), like)));
        }
        return spec;
    }

    public static Specification<Finding> findingMatches(String organizationId, String query, String auditId,
                                                        Severity severity, Boolean resolved) {
        Specification<Finding> spec = inOrganization(organizationId);
        if (auditId != null && !auditId.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("auditId"), auditId));
        }
        if (severity != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (resolved != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("resolved"), resolved));
        }
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("id")), like),
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("owner")), like)));
        }
        return spec;
    }

    public static <T> Specification<T> inOrganization(String organizationId) {
        Objects.requireNonNull(organizationId, "organizationId");
        return (root, q, cb) -> cb.equal(root.get("organizationId"), organizationId);
    }
}
