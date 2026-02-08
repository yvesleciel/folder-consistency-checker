package com.linagora.consistency.domain.port.driven;

import com.linagora.consistency.domain.model.Email;

import java.util.List;

/**
 * Secondary port (driven) for retrieving users from external data source.
 * Framework-agnostic.
 */
public interface ForRetrievingUsers {

    /**
     * Retrieves all user email addresses.
     *
     * @return list of user emails
     */
    List<Email> retrieveAllUsers();
}
