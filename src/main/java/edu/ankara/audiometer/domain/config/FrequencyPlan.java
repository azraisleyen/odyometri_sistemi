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
     * Runtime progression order is duplicate-safe. The configured 1000 Hz retest is preserved in
     * clinicalOrder for documentation/configuration, while the educational runtime avoids duplicate
     * threshold rows when retesting is disabled.
     */
    public List<FrequencyHz> activeOrder() {
        Stream<FrequencyHz> base = clinicalOrder.stream();
        Stream<FrequencyHz> all = enableInterOctaves
                ? Stream.concat(base, optionalInterOctaves.stream())
                : base;
        return List.copyOf(new LinkedHashSet<>(all.toList()));
    }

    private static List<FrequencyHz> vals(int... values) {
        return Arrays.stream(values).mapToObj(FrequencyHz::new).toList();
    }
}
