package com.auditapp.audit.web.dto;

import com.auditapp.audit.domain.EntityType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Body for creating an organization or replacing its details. Only legalName is required; blank optional fields
 * are stored as null. Status and isActive are not part of it: members can't change them.
 */
public record OrganizationDetailsRequest(
        @NotBlank @Size(max = 255) String legalName,
        @Size(max = 255) String tradeName,
        EntityType entityType,
        @Size(max = 20) String industryCode,
        @Size(max = 4000) String businessPurpose,
        @PastOrPresent LocalDate dateOfIncorporation,
        @Email @Size(max = 255) String contactEmail,
        @Pattern(regexp = "^$|^\\+?[0-9 ()\\-]{5,30}$",
                message = "must be a phone number: digits, spaces, ( ) - and an optional leading +") String contactNumber,
        @Size(max = 1000) String address,
        @Min(1) @Max(12) Integer fiscalYearEndMonth) {

    public static OrganizationDetailsRequest withLegalName(String legalName) {
        return new OrganizationDetailsRequest(legalName, null, null, null, null, null, null, null, null, null);
    }
}
