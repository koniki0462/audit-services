package com.auditapp.audit.web;

import com.auditapp.audit.service.OrganizationService;
import com.auditapp.audit.web.dto.MemberRequest;
import com.auditapp.audit.web.dto.MemberResponse;
import com.auditapp.audit.web.dto.MemberRoleRequest;
import com.auditapp.audit.web.dto.NewMemberRequest;
import com.auditapp.audit.web.dto.OrganizationDetailsRequest;
import com.auditapp.audit.web.dto.OrganizationResponse;
import com.auditapp.audit.web.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code /organizations} creates new ones (application admins only); {@code /organization} is the organization of
 * the caller's token. Changing it or its members needs an organization admin, except leaving it.
 */
@RestController
@Tag(name = "Organizations")
public class OrganizationController {

    private final OrganizationService organizations;

    public OrganizationController(OrganizationService organizations) {
        this.organizations = organizations;
    }

    @PostMapping("/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Application admins: create an organization with the caller as its first member and admin, and get a token for it")
    public TokenResponse create(@Valid @RequestBody OrganizationDetailsRequest request) {
        return organizations.create(request);
    }

    @GetMapping("/organization")
    @Operation(summary = "Details of the current organization")
    public OrganizationResponse current() {
        return organizations.current();
    }

    @PutMapping("/organization")
    @Operation(summary = "Organization admins: replace the current organization's details (status and isActive can't be changed here)")
    public OrganizationResponse update(@Valid @RequestBody OrganizationDetailsRequest request) {
        return organizations.update(request);
    }

    @GetMapping("/organization/members")
    @Operation(summary = "List the members of the current organization")
    public List<MemberResponse> members() {
        return organizations.members();
    }

    @PostMapping("/organization/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Organization admins: create an account with a profile and initial password, and add it as a member")
    public MemberResponse createMember(@Valid @RequestBody NewMemberRequest request) {
        return organizations.createMember(request);
    }

    @PostMapping("/organization/members/existing")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Organization admins: add an existing account, by email, to the current organization")
    public MemberResponse addMember(@Valid @RequestBody MemberRequest request) {
        return organizations.addMember(request.email(), request.role());
    }

    @PatchMapping("/organization/members/{userId}")
    @Operation(summary = "Organization admins: change a member's role (the last organization admin can't be demoted)")
    public MemberResponse changeRole(@PathVariable String userId, @Valid @RequestBody MemberRoleRequest request) {
        return organizations.changeRole(userId, request.role());
    }

    @DeleteMapping("/organization/members/{userId}")
    @Operation(summary = "Remove a member (organization admins), or leave (anyone, for their own id)")
    public ResponseEntity<Void> removeMember(@PathVariable String userId) {
        organizations.removeMember(userId);
        return ResponseEntity.noContent().build();
    }
}
