package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.config.HughsonWestlakeConfig;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.ThresholdDecision;
import edu.ankara.audiometer.domain.model.TonePresentation;

import java.util.List;

public final class ThresholdDetector {
    private ThresholdDetector() {
    }

    /** Detects the lowest intensity satisfying the configured ascending-trial criterion. */
    public static ThresholdDecision detectThreshold(List<TonePresentation> presentations, HughsonWestlakeConfig config) {
        if (presentations.isEmpty()) {
            return ThresholdDecision.notReached(config.criterion(), "No presentations");
        }
        var last = presentations.getLast();
        var relevantAscendingTrials = presentations.stream()
                .filter(presentation -> presentation.ear() == last.ear())
                .filter(presentation -> presentation.frequency().equals(last.frequency()))
                .filter(TonePresentation::validAscendingTrial)
                .toList();

        return relevantAscendingTrials.stream()
                .map(TonePresentation::intensity)
                .distinct()
                .sorted()
                .map(intensity -> decisionForIntensity(intensity, relevantAscendingTrials, config))
                .filter(ThresholdDecision::reached)
                .findFirst()
                .orElse(ThresholdDecision.notReached(config.criterion(), "Criterion not yet satisfied"));
    }

    public static boolean isThresholdReached(List<TonePresentation> presentations, HughsonWestlakeConfig config) {
        return detectThreshold(presentations, config).reached();
    }

    private static ThresholdDecision decisionForIntensity(
            IntensityDbHL intensity,
            List<TonePresentation> relevantAscendingTrials,
            HughsonWestlakeConfig config
    ) {
        var trialsAtIntensity = relevantAscendingTrials.stream()
                .filter(presentation -> presentation.intensity().equals(intensity))
                .filter(presentation -> presentation.response().isPresent())
                .toList();
        int heardCount = (int) trialsAtIntensity.stream()
                .map(presentation -> presentation.response().orElse(PatientResponse.NOT_HEARD))
                .filter(PatientResponse::heard)
                .count();

        if (trialsAtIntensity.size() >= config.criterion().window() && heardCount >= config.criterion().required()) {
            return ThresholdDecision.reached(
                    intensity,
                    config.criterion(),
                    heardCount + "/" + trialsAtIntensity.size() + " ascending responses at " + intensity.value() + " dB HL"
            );
        }
        return ThresholdDecision.notReached(config.criterion(), "Not enough responses at " + intensity.value() + " dB HL");
    }
}
