package com.auditapp.audit.service;

import com.auditapp.audit.domain.MemberRole;
import com.auditapp.audit.domain.Membership;
import com.auditapp.audit.repository.MembershipRepository;
import com.auditapp.audit.repository.UserAccountRepository;
import com.auditapp.audit.security.CurrentUser;
import org.springframework.stereotype.Component;

/**
 * Role checks for the caller. Roles are read from the database on every check, not from the token, so a change
 * takes effect on the next request. An application admin counts as an organization admin in every organization
 * they belong to, but gets no access to organizations they don't belong to.
 */
@Component
public class Permissions {

    private final UserAccountRepository users;
    private final MembershipRepository memberships;
    private final CurrentUser currentUser;

    Permissions(UserAccountRepository users, MembershipRepository memberships, CurrentUser currentUser) {
        this.users = users;
        this.memberships = memberships;
        this.currentUser = currentUser;
    }

    public boolean isApplicationAdmin() {
        return users.findById(currentUser.userId()).map(u -> u.isApplicationAdmin()).orElse(false);
    }

    public boolean isOrganizationAdmin() {
        MemberRole role = memberships.findByUserIdAndOrganizationId(currentUser.userId(), currentUser.organizationId())
                .map(Membership::getRole).orElse(null);
        return role == MemberRole.ORG_ADMIN || isApplicationAdmin();
    }

    public void requireApplicationAdmin() {
        if (!isApplicationAdmin()) {
            throw new ForbiddenException("Only an application admin can do this.");
        }
    }

    public void requireOrganizationAdmin() {
        if (!isOrganizationAdmin()) {
            throw new ForbiddenException("Only an organization admin can do this.");
        }
    }
}
