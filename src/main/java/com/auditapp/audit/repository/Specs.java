package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Audit;
import com.auditapp.audit.domain.AuditStatus;
import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Optional filters for the list endpoints. A null argument means no filter. */
public final class Specs {

    private Specs() {
    }

    /** Matches like the front end's filterAudits: case-insensitive substring of id, title, department or auditor. */
    public static Specification<Audit> auditMatches(String query, AuditStatus status) {
        Specification<Audit> spec = (root, q, cb) -> cb.conjunction();
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

    public static Specification<Finding> findingMatches(String query, String auditId, Severity severity, Boolean resolved) {
        Specification<Finding> spec = (root, q, cb) -> cb.conjunction();
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
}
