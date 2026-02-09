package com.linagora.consistency.adapter.driving.dto;

import java.util.List;

/**
 * DTO for complete inconsistency report API response.
 */
public record InconsistencyReportDto(
    InconsistencySummaryDto summary,
    List<InconsistencyDto> inconsistencies
) {
}
