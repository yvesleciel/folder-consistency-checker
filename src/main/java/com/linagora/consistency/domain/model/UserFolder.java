package com.linagora.consistency.domain.model;

import java.util.Objects;

/**
 * Entity representing a folder owned by a user from user-specific endpoint.
 * Immutable.
 */
public final class UserFolder {

    private final FolderId id;
    private final FolderName name;

    private UserFolder(FolderId id, FolderName name) {
        this.id = Objects.requireNonNull(id, "FolderId cannot be null");
        this.name = Objects.requireNonNull(name, "FolderName cannot be null");
    }

    public static UserFolder of(FolderId id, FolderName name) {
        return new UserFolder(id, name);
    }

    public FolderId getId() {
        return id;
    }

    public FolderName getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFolder that = (UserFolder) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "UserFolder{id=" + id + ", name=" + name + "}";
    }
}
