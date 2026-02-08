package com.linagora.consistency.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate representing all folders for a specific user.
 * Immutable.
 */
public final class UserFolders {

    private final Email userEmail;
    private final List<UserFolder> folders;

    private UserFolders(Email userEmail, List<UserFolder> folders) {
        this.userEmail = Objects.requireNonNull(userEmail, "Email cannot be null");
        this.folders = Collections.unmodifiableList(
            Objects.requireNonNull(folders, "Folders list cannot be null")
        );
    }

    public static UserFolders of(Email userEmail, List<UserFolder> folders) {
        return new UserFolders(userEmail, folders);
    }

    public Email getUserEmail() {
        return userEmail;
    }

    public List<UserFolder> getFolders() {
        return folders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFolders that = (UserFolders) o;
        return Objects.equals(userEmail, that.userEmail) && Objects.equals(folders, that.folders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userEmail, folders);
    }

    @Override
    public String toString() {
        return "UserFolders{user=" + userEmail + ", folderCount=" + folders.size() + "}";
    }
}
