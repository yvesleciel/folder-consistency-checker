package com.linagora.consistency.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Entity representing a detected inconsistency between data sources.
 * Immutable.
 */
public final class Inconsistency {

    private final InconsistencyType type;
    private final FolderId folderId;
    private final Email userEmail;
    private final Optional<FolderName> globalFolderName;
    private final Optional<FolderName> userFolderName;

    private Inconsistency(
        InconsistencyType type,
        FolderId folderId,
        Email userEmail,
        Optional<FolderName> globalFolderName,
        Optional<FolderName> userFolderName
    ) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.folderId = Objects.requireNonNull(folderId, "FolderId cannot be null");
        this.userEmail = Objects.requireNonNull(userEmail, "Email cannot be null");
        this.globalFolderName = Objects.requireNonNull(globalFolderName, "globalFolderName cannot be null");
        this.userFolderName = Objects.requireNonNull(userFolderName, "userFolderName cannot be null");
    }

    public static Inconsistency nameMismatch(
        FolderId folderId,
        Email userEmail,
        FolderName globalName,
        FolderName userName
    ) {
        return new Inconsistency(
            InconsistencyType.NAME_MISMATCH,
            folderId,
            userEmail,
            Optional.of(globalName),
            Optional.of(userName)
        );
    }

    public static Inconsistency missingInGlobal(
        FolderId folderId,
        Email userEmail,
        FolderName userName
    ) {
        return new Inconsistency(
            InconsistencyType.MISSING_IN_GLOBAL,
            folderId,
            userEmail,
            Optional.empty(),
            Optional.of(userName)
        );
    }

    public static Inconsistency missingInUserFolders(
        FolderId folderId,
        Email userEmail,
        FolderName globalName
    ) {
        return new Inconsistency(
            InconsistencyType.MISSING_IN_USER_FOLDERS,
            folderId,
            userEmail,
            Optional.of(globalName),
            Optional.empty()
        );
    }

    public InconsistencyType getType() {
        return type;
    }

    public FolderId getFolderId() {
        return folderId;
    }

    public Email getUserEmail() {
        return userEmail;
    }

    public Optional<FolderName> getGlobalFolderName() {
        return globalFolderName;
    }

    public Optional<FolderName> getUserFolderName() {
        return userFolderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inconsistency that = (Inconsistency) o;
        return type == that.type
            && Objects.equals(folderId, that.folderId)
            && Objects.equals(userEmail, that.userEmail)
            && Objects.equals(globalFolderName, that.globalFolderName)
            && Objects.equals(userFolderName, that.userFolderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, folderId, userEmail, globalFolderName, userFolderName);
    }

    @Override
    public String toString() {
        return "Inconsistency{" +
            "type=" + type +
            ", folderId=" + folderId +
            ", user=" + userEmail +
            ", globalName=" + globalFolderName.map(FolderName::getValue).orElse("N/A") +
            ", userName=" + userFolderName.map(FolderName::getValue).orElse("N/A") +
            '}';
    }
}
