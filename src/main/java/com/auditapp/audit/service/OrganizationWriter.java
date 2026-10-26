package com.auditapp.audit.service;

import com.auditapp.audit.domain.MemberRole;
import com.auditapp.audit.domain.Membership;
import com.auditapp.audit.domain.Organization;
import com.auditapp.audit.repository.MembershipRepository;
import com.auditapp.audit.repository.OrganizationRepository;
import com.auditapp.audit.web.dto.OrganizationDetailsRequest;
import org.springframework.stereotype.Component;

/** Creates organizations and applies their details. Shared by sign-up and the organization endpoints. */
@Component
class OrganizationWriter {

    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;

    OrganizationWriter(OrganizationRepository organizations, MembershipRepository memberships) {
        this.organizations = organizations;
        this.memberships = memberships;
    }

    /** Creates an organization with the given user as its first member and organization admin. Call inside a transaction. */
    Organization create(String userId, OrganizationDetailsRequest r) {
        String legalName = r.legalName().trim();
        if (organizations.existsByLegalNameIgnoreCase(legalName)) {
            throw duplicate();
        }
        Organization organization = new Organization(AuthService.newId(), legalName);
        apply(organization, r);
        organizations.save(organization);
        memberships.save(new Membership(AuthService.newId(), userId, organization.getId(), MemberRole.ORG_ADMIN));
        return organization;
    }

    /** Replaces an organization's details. Status and isActive are left as they are. */
    Organization update(Organization organization, OrganizationDetailsRequest r) {
        if (organizations.existsByLegalNameIgnoreCaseAndIdNot(r.legalName().trim(), organization.getId())) {
            throw duplicate();
        }
        apply(organization, r);
        return organizations.saveAndFlush(organization);
    }

    private static void apply(Organization o, OrganizationDetailsRequest r) {
        o.setLegalName(r.legalName().trim());
        o.setTradeName(blankToNull(r.tradeName()));
        o.setEntityType(r.entityType());
        o.setIndustryCode(blankToNull(r.industryCode()));
        o.setBusinessPurpose(blankToNull(r.businessPurpose()));
        o.setDateOfIncorporation(r.dateOfIncorporation());
        String email = blankToNull(r.contactEmail());
        o.setContactEmail(email == null ? null : AuthService.normaliseEmail(email));
        o.setContactNumber(blankToNull(r.contactNumber()));
        o.setAddress(blankToNull(r.address()));
        o.setFiscalYearEndMonth(r.fiscalYearEndMonth());
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ConflictException duplicate() {
        return new ConflictException("An organization with this legal name already exists.");
    }
}
