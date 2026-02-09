package com.linagora.consistency.adapter.driving.dto;

/**
 * DTO for API response representing a single inconsistency.
 */
public record InconsistencyDto(
    String type,
    String folderId,
    String userEmail,
    String globalFolderName,
    String userFolderName
) {
}
