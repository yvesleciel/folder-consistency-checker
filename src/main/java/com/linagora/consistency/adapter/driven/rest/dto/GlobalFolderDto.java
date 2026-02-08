package com.linagora.consistency.adapter.driven.rest.dto;

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
