package com.linagora.consistency.adapter.driving.dto;

import java.util.Map;

/**
 * DTO for summary statistics in the inconsistency report.
 */
public record InconsistencySummaryDto(
    int totalInconsistencies,
    Map<String, Long> countsByType
) {
}
