package com.auditapp.audit.service;

import static com.auditapp.audit.service.OrganizationWriter.blankToNull;

import com.auditapp.audit.domain.MemberRole;
import com.auditapp.audit.domain.Membership;
import com.auditapp.audit.domain.Organization;
import com.auditapp.audit.domain.UserAccount;
import com.auditapp.audit.repository.MembershipRepository;
import com.auditapp.audit.repository.OrganizationRepository;
import com.auditapp.audit.repository.UserAccountRepository;
import com.auditapp.audit.security.CurrentUser;
import com.auditapp.audit.web.dto.MemberResponse;
import com.auditapp.audit.web.dto.NewMemberRequest;
import com.auditapp.audit.web.dto.OrganizationDetailsRequest;
import com.auditapp.audit.web.dto.OrganizationResponse;
import com.auditapp.audit.web.dto.TokenResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Organizations and their members. Only an application admin creates organizations here (anyone can through
 * sign-up), and only an organization admin edits the organization or manages its members. Every organization keeps
 * at least one organization admin.
 */
@Service
@Transactional(readOnly = true)
public class OrganizationService {

    private final UserAccountRepository users;
    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final OrganizationWriter writer;
    private final AuthService auth;
    private final CurrentUser currentUser;
    private final Permissions permissions;

    OrganizationService(UserAccountRepository users, OrganizationRepository organizations, MembershipRepository memberships,
                        OrganizationWriter writer, AuthService auth, CurrentUser currentUser, Permissions permissions) {
        this.users = users;
        this.organizations = organizations;
        this.memberships = memberships;
        this.writer = writer;
        this.auth = auth;
        this.currentUser = currentUser;
        this.permissions = permissions;
    }

    /** Creates an organization with the caller as its first member and admin, and returns a token for it. */
    @Transactional
    public TokenResponse create(OrganizationDetailsRequest request) {
        permissions.requireApplicationAdmin();
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
        permissions.requireOrganizationAdmin();
        return OrganizationResponse.from(writer.update(find(), request));
    }

    private Organization find() {
        String id = currentUser.organizationId();
        return organizations.findById(id).orElseThrow(() -> new NotFoundException("Organization", id));
    }

    public List<MemberResponse> members() {
        String organizationId = currentUser.organizationId();
        Map<String, MemberRole> roles = memberships.findByOrganizationId(organizationId).stream()
                .collect(Collectors.toMap(Membership::getUserId, Membership::getRole));
        return users.findMembers(organizationId).stream().map(u -> MemberResponse.from(u, roles.get(u.getId()))).toList();
    }

    /** Creates an account with the given profile and password, and adds it to the current organization. */
    @Transactional
    public MemberResponse createMember(NewMemberRequest r) {
        permissions.requireOrganizationAdmin();
        String email = AuthService.normaliseEmail(r.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("An account already uses " + email + ". Add it as an existing account instead.");
        }
        String firstName = r.firstName().trim();
        String lastName = r.lastName().trim();
        UserAccount user = new UserAccount(AuthService.newId(), email, firstName + " " + lastName,
                auth.encodePassword(r.password()));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setGender(r.gender());
        user.setPhoneNumber(blankToNull(r.phoneNumber()));
        user.setAddress(blankToNull(r.address()));
        users.save(user);
        return join(user, r.role());
    }

    /** Adds an existing account to the current organization. */
    @Transactional
    public MemberResponse addMember(String email, MemberRole role) {
        permissions.requireOrganizationAdmin();
        UserAccount user = users.findByEmail(AuthService.normaliseEmail(email))
                .orElseThrow(() -> new NotFoundException("No account uses " + email.trim() + ". Create the member instead."));
        if (memberships.existsByUserIdAndOrganizationId(user.getId(), currentUser.organizationId())) {
            throw new ConflictException(user.getEmail() + " is already a member of this organization.");
        }
        return join(user, role);
    }

    private MemberResponse join(UserAccount user, MemberRole role) {
        MemberRole granted = role == null ? MemberRole.MEMBER : role;
        memberships.save(new Membership(AuthService.newId(), user.getId(), currentUser.organizationId(), granted));
        return MemberResponse.from(user, granted);
    }

    /** Makes a member an organization admin, or a plain member. */
    @Transactional
    public MemberResponse changeRole(String userId, MemberRole role) {
        permissions.requireOrganizationAdmin();
        Membership membership = findMembership(userId);
        if (role != MemberRole.ORG_ADMIN) {
            requireAnotherAdmin(membership, "An organization must keep at least one organization admin.");
        }
        membership.setRole(role);
        return MemberResponse.from(auth.requireUser(userId), role);
    }

    /**
     * Removes a member. Organization admins can remove anyone; any member can remove themselves (leave). Their
     * tokens for this organization stop working on the next request.
     */
    @Transactional
    public void removeMember(String userId) {
        if (!userId.equals(currentUser.userId())) {
            permissions.requireOrganizationAdmin();
        }
        Membership membership = findMembership(userId);
        if (memberships.countByOrganizationId(currentUser.organizationId()) <= 1) {
            throw new ConflictException("An organization must keep at least one member.");
        }
        requireAnotherAdmin(membership,
                "An organization must keep at least one organization admin. Make someone else an admin first.");
        memberships.delete(membership);
    }

    private Membership findMembership(String userId) {
        return memberships.findByUserIdAndOrganizationId(userId, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("Member", userId));
    }

    /** Refuses to take away the organization's last organization admin. */
    private void requireAnotherAdmin(Membership membership, String message) {
        if (membership.getRole() == MemberRole.ORG_ADMIN
                && memberships.countByOrganizationIdAndRole(membership.getOrganizationId(), MemberRole.ORG_ADMIN) <= 1) {
            throw new ConflictException(message);
        }
    }
}
