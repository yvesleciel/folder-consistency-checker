package com.linagora.consistency.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a folder unique identifier.
 * Immutable and validates UUID format.
 */
public final class FolderId {

    private final String value;

    private FolderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FolderId cannot be null or blank");
        }
        // Validate UUID format
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value, e);
        }
        this.value = value;
    }

    public static FolderId of(String value) {
        return new FolderId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderId folderId = (FolderId) o;
        return Objects.equals(value, folderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
