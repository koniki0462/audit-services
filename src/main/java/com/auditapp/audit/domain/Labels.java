package com.auditapp.audit.domain;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Labels {

    private Labels() {
    }

    static <E extends Enum<E>> E parse(Class<E> type, String value, Function<E, String> label) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        for (E e : type.getEnumConstants()) {
            if (label.apply(e).equalsIgnoreCase(v) || e.name().equalsIgnoreCase(v)) {
                return e;
            }
        }
        String allowed = Arrays.stream(type.getEnumConstants()).map(label).collect(Collectors.joining(", "));
        throw new IllegalArgumentException("Invalid " + type.getSimpleName() + " '" + value + "'. Allowed: " + allowed);
    }
}
