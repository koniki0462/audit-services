package com.auditapp.audit.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Stored by enum name; serialised to JSON with the labels the front end uses ("In Progress"). */
public enum AuditStatus {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("In Review"),
    COMPLETED("Completed");

    private final String label;

    AuditStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    /** Accepts the label ("In Progress") or the enum name ("IN_PROGRESS"), ignoring case. */
    @JsonCreator
    public static AuditStatus from(String value) {
        return Labels.parse(AuditStatus.class, value, AuditStatus::label);
    }
}
