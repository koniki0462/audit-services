package com.auditapp.audit.service;

import com.auditapp.audit.domain.Audit;
import com.auditapp.audit.domain.AuditStatus;
import com.auditapp.audit.repository.AuditRepository;
import com.auditapp.audit.repository.FindingRepository;
import com.auditapp.audit.repository.Specs;
import com.auditapp.audit.web.dto.AuditRequest;
import com.auditapp.audit.web.dto.AuditResponse;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditService {

    private final AuditRepository audits;
    private final FindingRepository findings;

    public AuditService(AuditRepository audits, FindingRepository findings) {
        this.audits = audits;
        this.findings = findings;
    }

    public List<AuditResponse> list(String query, AuditStatus status) {
        return audits.findAll(Specs.auditMatches(query, status), Sort.by("id")).stream()
                .map(AuditResponse::from)
                .toList();
    }

    public AuditResponse get(String id) {
        return AuditResponse.from(find(id));
    }

    @Transactional
    public AuditResponse create(AuditRequest request) {
        Audit audit = new Audit("AUD-" + audits.nextSequence());
        apply(audit, request);
        return AuditResponse.from(audits.saveAndFlush(audit));
    }

    @Transactional
    public AuditResponse update(String id, AuditRequest request) {
        Audit audit = find(id);
        apply(audit, request);
        return AuditResponse.from(audits.saveAndFlush(audit));
    }

    /** Deletes the audit and all of its findings. */
    @Transactional
    public void delete(String id) {
        Audit audit = find(id);
        findings.deleteByAuditId(id);
        audits.delete(audit);
    }

    Audit find(String id) {
        return audits.findById(id).orElseThrow(() -> new NotFoundException("Audit", id));
    }

    private static void apply(Audit audit, AuditRequest r) {
        audit.setTitle(r.title().trim());
        audit.setDepartment(r.department().trim());
        audit.setAuditor(r.auditor().trim());
        audit.setStatus(r.status());
        audit.setStartDate(r.startDate());
        audit.setEndDate(r.endDate());
        audit.setProgress(r.progress());
        audit.setDescription(r.description() == null ? "" : r.description().trim());
    }
}
