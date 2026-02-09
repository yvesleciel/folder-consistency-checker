package com.linagora.consistency.adapter.driven.dto;

/**
 * DTO for user folder from REST API.
 * Used for JSON deserialization.
 */
public record UserFolderDto(
    String id,
    String name
) {
}
