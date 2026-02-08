package com.linagora.consistency.domain.port.driven;

import com.linagora.consistency.domain.model.Email;
import com.linagora.consistency.domain.model.UserFolders;

/**
 * Secondary port (driven) for retrieving user-specific folders from external data source.
 * Framework-agnostic.
 */
public interface ForRetrievingUserFolders {

    /**
     * Retrieves all folders for a specific user.
     *
     * @param userEmail the user's email address
     * @return user folders aggregate
     */
    UserFolders retrieveFoldersForUser(Email userEmail);
}
