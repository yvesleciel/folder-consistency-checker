package com.linagora.consistency.domain.port.driving;

import com.linagora.consistency.domain.model.InconsistencyReport;

/**
 * Primary port (driving) for detecting folder inconsistencies.
 * This is the main use case interface exposed by the domain.
 * Framework-agnostic.
 */
public interface ForDetectingInconsistencies {

    /**
     * Detects all inconsistencies between user-specific folders and global folders.
     *
     * @return a report containing all detected inconsistencies
     */
    InconsistencyReport detectInconsistencies();
}
