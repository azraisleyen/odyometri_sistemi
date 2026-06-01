package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.config.HughsonWestlakeConfig;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.ThresholdDecision;
import edu.ankara.audiometer.domain.model.TonePresentation;

import java.util.Comparator;
import java.util.List;

public final class ThresholdDetector {
    private ThresholdDetector() {
    }

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
                .sorted(Comparator.naturalOrder())
                .map(intensity -> decisionForIntensity(intensity, relevantAscendingTrials, config))
                .filter(ThresholdDecision::reached)
                .findFirst()
                .orElse(ThresholdDecision.notReached(config.criterion(), "Criterion not yet satisfied"));
    }

    private static ThresholdDecision decisionForIntensity(
            IntensityDbHL intensity,
            List<TonePresentation> relevantAscendingTrials,
            HughsonWestlakeConfig config
    ) {
        var trials = relevantAscendingTrials.stream()
                .filter(presentation -> presentation.intensity().equals(intensity))
                .filter(presentation -> presentation.response().isPresent())
                .toList();
        int heard = (int) trials.stream()
                .filter(presentation -> presentation.response().orElseThrow().heard())
                .count();

        if (trials.size() >= config.criterion().window && heard >= config.criterion().required) {
            return ThresholdDecision.reached(
                    intensity,
                    config.criterion(),
                    heard + "/" + trials.size() + " ascending responses at " + intensity.value() + " dB HL"
            );
        }
        return ThresholdDecision.notReached(config.criterion(), "not enough at " + intensity.value());
    }

    public static boolean isThresholdReached(List<TonePresentation> presentations, HughsonWestlakeConfig config) {
        return detectThreshold(presentations, config).reached();
    }
}
