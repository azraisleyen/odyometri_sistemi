package edu.ankara.audiometer.domain.config;

import edu.ankara.audiometer.domain.model.FrequencyHz;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

public record FrequencyPlan(
        List<FrequencyHz> requiredFrequencies,
        List<FrequencyHz> clinicalOrder,
        List<FrequencyHz> optionalInterOctaves,
        boolean enableInterOctaves,
        int interOctaveDeltaDb
) {
    public FrequencyPlan {
        requiredFrequencies = List.copyOf(requiredFrequencies);
        clinicalOrder = List.copyOf(clinicalOrder);
        optionalInterOctaves = List.copyOf(optionalInterOctaves);
    }

    public static FrequencyPlan defaults() {
        return new FrequencyPlan(
                vals(250, 500, 1000, 2000, 4000, 8000),
                vals(1000, 2000, 4000, 8000, 1000, 500, 250),
                vals(750, 1500, 3000, 6000),
                false,
                20
        );
    }

    /**
     * Runtime Hughson-Westlake sequence. It intentionally preserves the configured duplicate
     * 1000 Hz occurrence so that the second 1000 Hz presentation is a true retest step.
     */
    public List<FrequencyHz> activeOrder() {
        return enableInterOctaves
                ? Stream.concat(clinicalOrder.stream(), optionalInterOctaves.stream()).toList()
                : clinicalOrder;
    }

    /** Unique final audiogram frequencies. Duplicate retest occurrences are not final rows. */
    public List<FrequencyHz> uniqueThresholdFrequencies() {
        return List.copyOf(new LinkedHashSet<>(activeOrder()));
    }

    /** Backward-compatible alias for unique final threshold/audiogram frequencies. */
    public List<FrequencyHz> activeThresholdOrder() {
        return uniqueThresholdFrequencies();
    }

    public boolean isRetestOccurrence(int orderIndex) {
        if (orderIndex < 0 || orderIndex >= activeOrder().size()) {
            return false;
        }
        return firstOccurrenceIndex(activeOrder().get(orderIndex)) < orderIndex;
    }

    public int firstOccurrenceIndex(FrequencyHz frequency) {
        return activeOrder().indexOf(frequency);
    }

    private static List<FrequencyHz> vals(int... values) {
        return Arrays.stream(values).mapToObj(FrequencyHz::new).toList();
    }
}
