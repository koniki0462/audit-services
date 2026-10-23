package com.auditapp.audit.service;

import com.auditapp.audit.domain.AuditStatus;
import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import com.auditapp.audit.repository.AuditRepository;
import com.auditapp.audit.repository.FindingRepository;
import com.auditapp.audit.repository.Specs;
import com.auditapp.audit.security.CurrentUser;
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
    private final CurrentUser currentUser;

    public DashboardService(AuditRepository audits, FindingRepository findings, CurrentUser currentUser) {
        this.audits = audits;
        this.findings = findings;
        this.currentUser = currentUser;
    }

    public DashboardSummary summary() {
        String org = currentUser.organizationId();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (AuditStatus s : AuditStatus.values()) {
            byStatus.put(s.label(), audits.countByOrganizationIdAndStatus(org, s));
        }
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Severity s : Severity.values()) {
            Specification<Finding> openWithSeverity = Specs.<Finding>inOrganization(org).and((root, q, cb) -> cb.and(
                    cb.isFalse(root.get("resolved")), cb.equal(root.get("severity"), s)));
            bySeverity.put(s.label(), findings.count(openWithSeverity));
        }
        return new DashboardSummary(
                audits.countByOrganizationId(org),
                audits.countByOrganizationIdAndStatusIn(org, EnumSet.of(AuditStatus.IN_PROGRESS, AuditStatus.IN_REVIEW)),
                audits.countByOrganizationIdAndStatus(org, AuditStatus.COMPLETED),
                findings.countByOrganizationIdAndResolved(org, false),
                findings.countByOrganizationIdAndResolvedFalseAndSeverityIn(org, EnumSet.of(Severity.HIGH, Severity.CRITICAL)),
                byStatus,
                bySeverity);
    }
}
