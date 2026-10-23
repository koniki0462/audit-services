package com.auditapp.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/** Links a user to an organization they can work in. Immutable: removing a member deletes the row. */
@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private String organizationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Membership() {
    }

    public Membership(String id, String userId, String organizationId) {
        this.id = id;
        this.userId = userId;
        this.organizationId = organizationId;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getOrganizationId() { return organizationId; }
    public Instant getCreatedAt() { return createdAt; }
}
