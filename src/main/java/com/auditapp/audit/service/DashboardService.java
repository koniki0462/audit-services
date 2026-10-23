package com.auditapp.audit.service;

import com.auditapp.audit.domain.AuditStatus;
import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import com.auditapp.audit.repository.AuditRepository;
import com.auditapp.audit.repository.FindingRepository;
import com.auditapp.audit.web.dto.DashboardSummary;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final AuditRepository audits;
    private final FindingRepository findings;

    public DashboardService(AuditRepository audits, FindingRepository findings) {
        this.audits = audits;
        this.findings = findings;
    }

    public DashboardSummary summary() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (AuditStatus s : AuditStatus.values()) {
            byStatus.put(s.label(), audits.countByStatus(s));
        }
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Severity s : Severity.values()) {
            Specification<Finding> openWithSeverity = (root, q, cb) -> cb.and(
                    cb.isFalse(root.get("resolved")), cb.equal(root.get("severity"), s));
            bySeverity.put(s.label(), findings.count(openWithSeverity));
        }
        return new DashboardSummary(
                audits.count(),
                audits.countByStatusIn(EnumSet.of(AuditStatus.IN_PROGRESS, AuditStatus.IN_REVIEW)),
                audits.countByStatus(AuditStatus.COMPLETED),
                findings.countByResolved(false),
                findings.countByResolvedFalseAndSeverityIn(EnumSet.of(Severity.HIGH, Severity.CRITICAL)),
                byStatus,
                bySeverity);
    }
}
