package com.auditapp.audit.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * What a member may do in one organization. Every member works with audits and findings; an organization admin
 * also edits the organization and manages its members. Stored by enum name; serialised with its label.
 */
public enum MemberRole {
    ORG_ADMIN("Organization Admin"),
    MEMBER("Member");

    private final String label;

    MemberRole(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static MemberRole from(String value) {
        return Labels.parse(MemberRole.class, value, MemberRole::label);
    }
}
