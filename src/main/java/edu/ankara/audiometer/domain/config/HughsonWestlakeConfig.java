package edu.ankara.audiometer.domain.config;

import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;

public record HughsonWestlakeConfig(
        IntensityDbHL startIntensity,
        int heardDecreaseDb,
        int notHeardIncreaseDb,
        int toneDurationMs,
        int responseTimeoutMs,
        ThresholdCriterion criterion
) {
    public static HughsonWestlakeConfig defaults() {
        return new HughsonWestlakeConfig(
                new IntensityDbHL(40),
                10,
                5,
                1000,
                2500,
                ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING
        );
    }

    public HughsonWestlakeConfig withCriterion(ThresholdCriterion nextCriterion) {
        return new HughsonWestlakeConfig(
                startIntensity,
                heardDecreaseDb,
                notHeardIncreaseDb,
                toneDurationMs,
                responseTimeoutMs,
                nextCriterion
        );
    }
}
