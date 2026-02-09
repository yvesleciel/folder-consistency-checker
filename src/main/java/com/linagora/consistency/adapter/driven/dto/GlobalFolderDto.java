package com.linagora.consistency.adapter.driven.dto;

/**
 * DTO for global folder from REST API.
 * Used for JSON deserialization.
 */
public record GlobalFolderDto(
    String id,
    String user,
    String name
) {
}
