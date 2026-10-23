package com.auditapp.audit.service;

import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import com.auditapp.audit.repository.AuditRepository;
import com.auditapp.audit.repository.FindingRepository;
import com.auditapp.audit.repository.Specs;
import com.auditapp.audit.security.CurrentUser;
import com.auditapp.audit.web.dto.FindingRequest;
import com.auditapp.audit.web.dto.FindingResponse;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FindingService {

    private final FindingRepository findings;
    private final AuditRepository audits;
    private final CurrentUser currentUser;

    public FindingService(FindingRepository findings, AuditRepository audits, CurrentUser currentUser) {
        this.findings = findings;
        this.audits = audits;
        this.currentUser = currentUser;
    }

    public List<FindingResponse> list(String query, String auditId, Severity severity, Boolean resolved) {
        return findings.findAll(Specs.findingMatches(currentUser.organizationId(), query, auditId, severity, resolved), Sort.by("id")).stream()
                .map(FindingResponse::from)
                .toList();
    }

    public List<FindingResponse> listForAudit(String auditId) {
        requireAudit(auditId);
        return findings.findByOrganizationIdAndAuditIdOrderByIdAsc(currentUser.organizationId(), auditId).stream().map(FindingResponse::from).toList();
    }

    public FindingResponse get(String id) {
        return FindingResponse.from(find(id));
    }

    @Transactional
    public FindingResponse create(FindingRequest request) {
        requireAudit(request.auditId());
        Finding finding = new Finding("F-" + findings.nextSequence(), currentUser.organizationId());
        apply(finding, request);
        return FindingResponse.from(findings.saveAndFlush(finding));
    }

    @Transactional
    public FindingResponse update(String id, FindingRequest request) {
        Finding finding = find(id);
        requireAudit(request.auditId());
        apply(finding, request);
        return FindingResponse.from(findings.saveAndFlush(finding));
    }

    @Transactional
    public FindingResponse setResolved(String id, boolean resolved) {
        Finding finding = find(id);
        finding.setResolved(resolved);
        return FindingResponse.from(findings.saveAndFlush(finding));
    }

    @Transactional
    public void delete(String id) {
        findings.delete(find(id));
    }

    private Finding find(String id) {
        return findings.findByIdAndOrganizationId(id, currentUser.organizationId()).orElseThrow(() -> new NotFoundException("Finding", id));
    }

    private void requireAudit(String auditId) {
        if (!audits.existsByIdAndOrganizationId(auditId, currentUser.organizationId())) {
            throw new NotFoundException("Audit", auditId);
        }
    }

    private static void apply(Finding finding, FindingRequest r) {
        finding.setAuditId(r.auditId());
        finding.setTitle(r.title().trim());
        finding.setSeverity(r.severity());
        finding.setOwner(r.owner().trim());
        finding.setDueDate(r.dueDate());
        finding.setResolved(Boolean.TRUE.equals(r.resolved()));
    }
}
