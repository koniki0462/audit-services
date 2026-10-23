package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.EntityType;
import com.auditapp.audit.domain.Organization;
import com.auditapp.audit.domain.OrganizationStatus;
import java.time.Instant;
import java.time.LocalDate;

public record OrganizationResponse(
        String id,
        String legalName,
        String tradeName,
        EntityType entityType,
        String industryCode,
        String businessPurpose,
        LocalDate dateOfIncorporation,
        String contactEmail,
        String contactNumber,
        String address,
        Integer fiscalYearEndMonth,
        OrganizationStatus status,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt) {

    public static OrganizationResponse from(Organization o) {
        return new OrganizationResponse(o.getId(), o.getLegalName(), o.getTradeName(), o.getEntityType(),
                o.getIndustryCode(), o.getBusinessPurpose(), o.getDateOfIncorporation(), o.getContactEmail(),
                o.getContactNumber(), o.getAddress(), o.getFiscalYearEndMonth(), o.getStatus(), o.isActive(),
                o.getCreatedAt(), o.getUpdatedAt());
    }
}
