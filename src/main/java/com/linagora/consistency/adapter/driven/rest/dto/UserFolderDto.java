package com.linagora.consistency.adapter.driven.rest.dto;

/**
 * DTO for user folder from REST API.
 * Used for JSON deserialization.
 */
public record UserFolderDto(
    String id,
    String name
) {
}
