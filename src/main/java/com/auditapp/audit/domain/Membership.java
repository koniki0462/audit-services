package com.auditapp.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/** Links a user to an organization they can work in, with their role there. Removing a member deletes the row. */
@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private String organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Membership() {
    }

    public Membership(String id, String userId, String organizationId, MemberRole role) {
        this.id = id;
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getOrganizationId() { return organizationId; }
    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
}
