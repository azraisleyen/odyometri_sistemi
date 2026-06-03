package edu.ankara.audiometer.domain.model;

import java.util.Optional;

public record ThresholdDecision(
        boolean reached,
        Optional<IntensityDbHL> threshold,
        ThresholdCriterion criterion,
        String explanation
) {
    public static ThresholdDecision notReached(ThresholdCriterion criterion, String explanation) {
        return new ThresholdDecision(false, Optional.empty(), criterion, explanation);
    }

    public static ThresholdDecision reached(IntensityDbHL intensity, ThresholdCriterion criterion, String explanation) {
        return new ThresholdDecision(true, Optional.of(intensity), criterion, explanation);
    }
}
