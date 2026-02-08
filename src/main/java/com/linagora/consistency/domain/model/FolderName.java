package com.linagora.consistency.domain.model;

import java.util.Objects;

/**
 * Value Object representing a folder name.
 * Immutable.
 */
public final class FolderName {

    private final String value;

    private FolderName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FolderName cannot be null or blank");
        }
        this.value = value;
    }

    public static FolderName of(String value) {
        return new FolderName(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderName that = (FolderName) o;
        return Objects.equals(value, that.value);
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
