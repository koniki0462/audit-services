package com.auditapp.audit.web;

import com.auditapp.audit.domain.AuditStatus;
import com.auditapp.audit.service.AuditService;
import com.auditapp.audit.service.FindingService;
import com.auditapp.audit.web.dto.AuditRequest;
import com.auditapp.audit.web.dto.AuditResponse;
import com.auditapp.audit.web.dto.FindingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/audits")
@Tag(name = "Audits")
public class AuditController {

    private final AuditService audits;
    private final FindingService findings;

    public AuditController(AuditService audits, FindingService findings) {
        this.audits = audits;
        this.findings = findings;
    }

    @GetMapping
    @Operation(summary = "List audits, optionally filtered by text (q) and status")
    public List<AuditResponse> list(@RequestParam(required = false) String q,
                                    @RequestParam(required = false) AuditStatus status) {
        return audits.list(q, status);
    }

    @GetMapping("/{id}")
    public AuditResponse get(@PathVariable String id) {
        return audits.get(id);
    }

    @GetMapping("/{id}/findings")
    @Operation(summary = "List the findings raised in an audit")
    public List<FindingResponse> findings(@PathVariable String id) {
        return findings.listForAudit(id);
    }

    @PostMapping
    public ResponseEntity<AuditResponse> create(@Valid @RequestBody AuditRequest request) {
        AuditResponse created = audits.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public AuditResponse update(@PathVariable String id, @Valid @RequestBody AuditRequest request) {
        return audits.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an audit and all of its findings")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        audits.delete(id);
        return ResponseEntity.noContent().build();
    }
}
