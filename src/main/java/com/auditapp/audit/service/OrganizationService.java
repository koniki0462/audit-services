package com.auditapp.audit.service;

import com.auditapp.audit.domain.Membership;
import com.auditapp.audit.domain.Organization;
import com.auditapp.audit.domain.UserAccount;
import com.auditapp.audit.repository.MembershipRepository;
import com.auditapp.audit.repository.OrganizationRepository;
import com.auditapp.audit.repository.UserAccountRepository;
import com.auditapp.audit.security.CurrentUser;
import com.auditapp.audit.web.dto.OrganizationDetailsRequest;
import com.auditapp.audit.web.dto.OrganizationResponse;
import com.auditapp.audit.web.dto.TokenResponse;
import com.auditapp.audit.web.dto.UserResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Organizations and their members. There are no roles yet: every member can manage the organization's members. */
@Service
@Transactional(readOnly = true)
public class OrganizationService {

    private final UserAccountRepository users;
    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final OrganizationWriter writer;
    private final AuthService auth;
    private final CurrentUser currentUser;

    OrganizationService(UserAccountRepository users, OrganizationRepository organizations, MembershipRepository memberships,
                        OrganizationWriter writer, AuthService auth, CurrentUser currentUser) {
        this.users = users;
        this.organizations = organizations;
        this.memberships = memberships;
        this.writer = writer;
        this.auth = auth;
        this.currentUser = currentUser;
    }

    /** Creates an organization with the caller as its first member, and returns a token for it. */
    @Transactional
    public TokenResponse create(OrganizationDetailsRequest request) {
        UserAccount user = auth.requireUser(currentUser.userId());
        Organization organization = writer.create(user.getId(), request);
        return auth.tokenFor(user, organization);
    }

    public OrganizationResponse current() {
        return OrganizationResponse.from(find());
    }

    /** Replaces the current organization's details. Status and isActive are left as they are. */
    @Transactional
    public OrganizationResponse update(OrganizationDetailsRequest request) {
        return OrganizationResponse.from(writer.update(find(), request));
    }

    private Organization find() {
        String id = currentUser.organizationId();
        return organizations.findById(id).orElseThrow(() -> new NotFoundException("Organization", id));
    }

    public List<UserResponse> members() {
        return users.findMembers(currentUser.organizationId()).stream().map(UserResponse::from).toList();
    }

    /** Adds an existing account to the current organization. */
    @Transactional
    public UserResponse addMember(String email) {
        String organizationId = currentUser.organizationId();
        UserAccount user = users.findByEmail(AuthService.normaliseEmail(email))
                .orElseThrow(() -> new NotFoundException("No account uses " + email.trim() + ". Ask them to sign up first."));
        if (memberships.existsByUserIdAndOrganizationId(user.getId(), organizationId)) {
            throw new ConflictException(user.getEmail() + " is already a member of this organization.");
        }
        memberships.save(new Membership(AuthService.newId(), user.getId(), organizationId));
        return UserResponse.from(user);
    }

    /** Removes a member. Their tokens for this organization stop working on the next request. */
    @Transactional
    public void removeMember(String userId) {
        String organizationId = currentUser.organizationId();
        Membership membership = memberships.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new NotFoundException("Member", userId));
        if (memberships.countByOrganizationId(organizationId) <= 1) {
            throw new ConflictException("An organization must keep at least one member.");
        }
        memberships.delete(membership);
    }
}
