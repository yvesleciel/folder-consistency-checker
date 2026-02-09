package com.linagora.consistency.adapter.driving;

import com.linagora.consistency.adapter.driving.dto.InconsistencyDto;
import com.linagora.consistency.adapter.driving.dto.InconsistencyReportDto;
import com.linagora.consistency.adapter.driving.dto.InconsistencySummaryDto;
import com.linagora.consistency.domain.model.Inconsistency;
import com.linagora.consistency.domain.model.InconsistencyReport;
import com.linagora.consistency.domain.model.InconsistencyType;
import com.linagora.consistency.domain.port.driving.ForDetectingInconsistencies;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller (driving adapter) exposing the inconsistency detection endpoint.
 * Implements reactive WebFlux controller returning Mono for non-blocking responses.
 */
@RestController
@RequestMapping("/inconsistencies")
public class InconsistencyController {

    private final ForDetectingInconsistencies inconsistencyDetector;

    public InconsistencyController(ForDetectingInconsistencies inconsistencyDetector) {
        this.inconsistencyDetector = inconsistencyDetector;
    }

    /**
     * GET /inconsistencies
     * Returns all detected inconsistencies between data sources.
     *
     * @return Mono of InconsistencyReportDto (reactive response)
     */
    @GetMapping
    public Mono<InconsistencyReportDto> getInconsistencies() {
        return Mono.fromCallable(inconsistencyDetector::detectInconsistencies)
            .map(this::toDto)
            .subscribeOn(Schedulers.boundedElastic()); // Run on separate thread pool
    }

    private InconsistencyReportDto toDto(InconsistencyReport report) {
        InconsistencySummaryDto summary = new InconsistencySummaryDto(
            report.getTotalCount(),
            convertCountsByType(report.getCountsByType())
        );

        List<InconsistencyDto> inconsistencies = report.getInconsistencies().stream()
            .map(this::toDto)
            .toList();

        return new InconsistencyReportDto(summary, inconsistencies);
    }

    private Map<String, Long> convertCountsByType(Map<InconsistencyType, Long> countsByType) {
        return countsByType.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().name(),
                Map.Entry::getValue
            ));
    }

    private InconsistencyDto toDto(Inconsistency inconsistency) {
        return new InconsistencyDto(
            inconsistency.getType().name(),
            inconsistency.getFolderId().getValue(),
            inconsistency.getUserEmail().getValue(),
            inconsistency.getGlobalFolderName()
                .map(name -> name.getValue())
                .orElse(null),
            inconsistency.getUserFolderName()
                .map(name -> name.getValue())
                .orElse(null)
        );
    }
}
