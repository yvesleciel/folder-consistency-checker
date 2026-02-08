package com.linagora.consistency.domain.port.driven;

import com.linagora.consistency.domain.model.GlobalFolder;

import java.util.List;

/**
 * Secondary port (driven) for retrieving global folders from external data source.
 * Framework-agnostic.
 */
public interface ForRetrievingGlobalFolders {

    /**
     * Retrieves all folders across all users.
     *
     * @return list of global folders
     */
    List<GlobalFolder> retrieveAllGlobalFolders();
}
