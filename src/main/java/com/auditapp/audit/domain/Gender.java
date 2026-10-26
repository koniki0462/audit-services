package com.auditapp.audit.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** A user's gender. Stored by enum name; serialised with its label ("Prefer not to say"). */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    UNDISCLOSED("Prefer not to say");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static Gender from(String value) {
        return Labels.parse(Gender.class, value, Gender::label);
    }
}
