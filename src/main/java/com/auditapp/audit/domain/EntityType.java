package com.auditapp.audit.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Legal form of an organization. Stored by enum name; serialised with its label ("Non-Profit"). */
public enum EntityType {
    SOLE_PROPRIETORSHIP("Sole Proprietorship"),
    PARTNERSHIP("Partnership"),
    LLC("LLC"),
    CORPORATION("Corporation"),
    NON_PROFIT("Non-Profit"),
    GOVERNMENT("Government"),
    OTHER("Other");

    private final String label;

    EntityType(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static EntityType from(String value) {
        return Labels.parse(EntityType.class, value, EntityType::label);
    }
}
