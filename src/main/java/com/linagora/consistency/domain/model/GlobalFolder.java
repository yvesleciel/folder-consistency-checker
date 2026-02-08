package com.linagora.consistency.domain.model;

import java.util.Objects;

/**
 * Entity representing a folder from global endpoint with user ownership.
 * Immutable.
 */
public final class GlobalFolder {

    private final FolderId id;
    private final Email userEmail;
    private final FolderName name;

    private GlobalFolder(FolderId id, Email userEmail, FolderName name) {
        this.id = Objects.requireNonNull(id, "FolderId cannot be null");
        this.userEmail = Objects.requireNonNull(userEmail, "Email cannot be null");
        this.name = Objects.requireNonNull(name, "FolderName cannot be null");
    }

    public static GlobalFolder of(FolderId id, Email userEmail, FolderName name) {
        return new GlobalFolder(id, userEmail, name);
    }

    public FolderId getId() {
        return id;
    }

    public Email getUserEmail() {
        return userEmail;
    }

    public FolderName getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalFolder that = (GlobalFolder) o;
        return Objects.equals(id, that.id)
            && Objects.equals(userEmail, that.userEmail)
            && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userEmail, name);
    }

    @Override
    public String toString() {
        return "GlobalFolder{id=" + id + ", user=" + userEmail + ", name=" + name + "}";
    }
}
