package com.auditapp.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** A tenant. Every audit and finding belongs to exactly one organization. */
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @Column(name = "organization_id")
    private String id;

    @Column(name = "legal_name", nullable = false, unique = true)
    private String legalName;

    @Column(name = "trade_name")
    private String tradeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 30)
    private EntityType entityType;

    @Column(name = "industry_code", length = 20)
    private String industryCode;

    @Column(name = "business_purpose", columnDefinition = "text")
    private String businessPurpose;

    @Column(name = "date_of_incorporation")
    private LocalDate dateOfIncorporation;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(length = 1000)
    private String address;

    @Column(name = "fiscal_year_end_month")
    private Integer fiscalYearEndMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Organization() {
    }

    public Organization(String id, String legalName) {
        this.id = id;
        this.legalName = legalName;
    }

    public String getId() { return id; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    public String getIndustryCode() { return industryCode; }
    public void setIndustryCode(String industryCode) { this.industryCode = industryCode; }
    public String getBusinessPurpose() { return businessPurpose; }
    public void setBusinessPurpose(String businessPurpose) { this.businessPurpose = businessPurpose; }
    public LocalDate getDateOfIncorporation() { return dateOfIncorporation; }
    public void setDateOfIncorporation(LocalDate dateOfIncorporation) { this.dateOfIncorporation = dateOfIncorporation; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getFiscalYearEndMonth() { return fiscalYearEndMonth; }
    public void setFiscalYearEndMonth(Integer fiscalYearEndMonth) { this.fiscalYearEndMonth = fiscalYearEndMonth; }
    public OrganizationStatus getStatus() { return status; }
    public void setStatus(OrganizationStatus status) { this.status = status; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
