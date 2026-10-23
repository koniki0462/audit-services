package com.auditapp.audit.web;

import com.auditapp.audit.service.OrganizationService;
import com.auditapp.audit.web.dto.MemberRequest;
import com.auditapp.audit.web.dto.OrganizationDetailsRequest;
import com.auditapp.audit.web.dto.OrganizationResponse;
import com.auditapp.audit.web.dto.TokenResponse;
import com.auditapp.audit.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** {@code /organizations} creates new ones; {@code /organization} is the organization of the caller's token. */
@RestController
@Tag(name = "Organizations")
public class OrganizationController {

    private final OrganizationService organizations;

    public OrganizationController(OrganizationService organizations) {
        this.organizations = organizations;
    }

    @PostMapping("/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an organization with the caller as its first member, and get a token for it")
    public TokenResponse create(@Valid @RequestBody OrganizationDetailsRequest request) {
        return organizations.create(request);
    }

    @GetMapping("/organization")
    @Operation(summary = "Details of the current organization")
    public OrganizationResponse current() {
        return organizations.current();
    }

    @PutMapping("/organization")
    @Operation(summary = "Replace the current organization's details (status and isActive can't be changed here)")
    public OrganizationResponse update(@Valid @RequestBody OrganizationDetailsRequest request) {
        return organizations.update(request);
    }

    @GetMapping("/organization/members")
    public List<UserResponse> members() {
        return organizations.members();
    }

    @PostMapping("/organization/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an existing account, by email, to the current organization")
    public UserResponse addMember(@Valid @RequestBody MemberRequest request) {
        return organizations.addMember(request.email());
    }

    @DeleteMapping("/organization/members/{userId}")
    @Operation(summary = "Remove a member from the current organization")
    public ResponseEntity<Void> removeMember(@PathVariable String userId) {
        organizations.removeMember(userId);
        return ResponseEntity.noContent().build();
    }
}
