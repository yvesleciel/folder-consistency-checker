package com.linagora.consistency.domain.model;

/**
 * Enumeration of all possible inconsistency types.
 */
public enum InconsistencyType {
    /**
     * Folder exists in both sources but with different names.
     */
    NAME_MISMATCH,

    /**
     * Folder exists in user-specific data but not in global data.
     */
    MISSING_IN_GLOBAL,

    /**
     * Folder exists in global data but not in user-specific data.
     */
    MISSING_IN_USER_FOLDERS
}
