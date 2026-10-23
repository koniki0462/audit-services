package com.auditapp.audit.web;

import com.auditapp.audit.domain.Severity;
import com.auditapp.audit.service.FindingService;
import com.auditapp.audit.web.dto.FindingRequest;
import com.auditapp.audit.web.dto.FindingResponse;
import com.auditapp.audit.web.dto.ResolutionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/findings")
@Tag(name = "Findings")
public class FindingController {

    private final FindingService findings;

    public FindingController(FindingService findings) {
        this.findings = findings;
    }

    @GetMapping
    @Operation(summary = "List findings, optionally filtered by text (q), audit, severity and resolved")
    public List<FindingResponse> list(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String auditId,
                                      @RequestParam(required = false) Severity severity,
                                      @RequestParam(required = false) Boolean resolved) {
        return findings.list(q, auditId, severity, resolved);
    }

    @GetMapping("/{id}")
    public FindingResponse get(@PathVariable String id) {
        return findings.get(id);
    }

    @PostMapping
    public ResponseEntity<FindingResponse> create(@Valid @RequestBody FindingRequest request) {
        FindingResponse created = findings.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public FindingResponse update(@PathVariable String id, @Valid @RequestBody FindingRequest request) {
        return findings.update(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Resolve or reopen a finding")
    public FindingResponse setResolved(@PathVariable String id, @Valid @RequestBody ResolutionRequest request) {
        return findings.setResolved(id, request.resolved());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        findings.delete(id);
        return ResponseEntity.noContent().build();
    }
}
