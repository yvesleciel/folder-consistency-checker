package com.linagora.consistency.domain.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregate representing the complete inconsistency analysis report.
 * Immutable.
 */
public final class InconsistencyReport {

    private final List<Inconsistency> inconsistencies;
    private final int totalCount;
    private final Map<InconsistencyType, Long> countsByType;

    private InconsistencyReport(List<Inconsistency> inconsistencies) {
        this.inconsistencies = Collections.unmodifiableList(
            Objects.requireNonNull(inconsistencies, "Inconsistencies cannot be null")
        );
        this.totalCount = inconsistencies.size();
        this.countsByType = calculateCountsByType(inconsistencies);
    }

    public static InconsistencyReport of(List<Inconsistency> inconsistencies) {
        return new InconsistencyReport(inconsistencies);
    }

    public static InconsistencyReport empty() {
        return new InconsistencyReport(Collections.emptyList());
    }

    private Map<InconsistencyType, Long> calculateCountsByType(List<Inconsistency> inconsistencies) {
        Map<InconsistencyType, Long> counts = inconsistencies.stream()
            .collect(Collectors.groupingBy(Inconsistency::getType, Collectors.counting()));
        return Collections.unmodifiableMap(counts);
    }

    public List<Inconsistency> getInconsistencies() {
        return inconsistencies;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public Map<InconsistencyType, Long> getCountsByType() {
        return countsByType;
    }

    public boolean hasInconsistencies() {
        return totalCount > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InconsistencyReport that = (InconsistencyReport) o;
        return Objects.equals(inconsistencies, that.inconsistencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inconsistencies);
    }

    @Override
    public String toString() {
        return "InconsistencyReport{total=" + totalCount + ", byType=" + countsByType + "}";
    }
}
