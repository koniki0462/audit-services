package com.auditapp.audit.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Lifecycle of an organization. Stored by enum name; serialised with its label ("Active"). */
public enum OrganizationStatus {
    PENDING("Pending"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    DISSOLVED("Dissolved");

    private final String label;

    OrganizationStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static OrganizationStatus from(String value) {
        return Labels.parse(OrganizationStatus.class, value, OrganizationStatus::label);
    }
}
