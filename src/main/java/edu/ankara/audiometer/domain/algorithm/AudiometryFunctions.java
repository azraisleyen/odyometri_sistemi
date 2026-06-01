package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.config.FrequencyPlan;
import edu.ankara.audiometer.domain.config.HughsonWestlakeConfig;
import edu.ankara.audiometer.domain.fp.Validation;
import edu.ankara.audiometer.domain.model.AudiogramPoint;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.PresentationDirection;
import edu.ankara.audiometer.domain.model.TestPhase;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.TonePresentation;
import edu.ankara.audiometer.domain.validation.AudiometryValidators;

import java.util.Optional;

public final class AudiometryFunctions {
    private AudiometryFunctions() {
    }

    public static Validation<FrequencyHz> validateFrequency(FrequencyHz frequency, AudiometryConfig config) {
        return AudiometryValidators.validateFrequency(frequency, config);
    }

    public static Validation<IntensityDbHL> validateIntensity(IntensityDbHL intensity, AudiometryConfig config) {
        return AudiometryValidators.validateIntensity(intensity, config);
    }

    public static IntensityDbHL nextIntensityAfterResponse(
            IntensityDbHL current,
            PatientResponse response,
            HughsonWestlakeConfig algorithm,
            AudiometryConfig config
    ) {
        int step = response.heard() ? -algorithm.heardDecreaseDb() : algorithm.notHeardIncreaseDb();
        return current.plus(step, config.minIntensity().value(), config.maxIntensity().value());
    }

    public static Optional<Integer> nextFrequencyIndex(TestState state) {
        int nextIndex = state.currentFrequencyIndex() + 1;
        return nextIndex < state.config().frequencyPlan().activeOrder().size()
                ? Optional.of(nextIndex)
                : Optional.empty();
    }

    /** Backward-compatible helper for callers that do not need duplicate-sensitive cursor state. */
    public static Optional<FrequencyHz> nextFrequency(FrequencyHz current, FrequencyPlan plan) {
        var order = plan.activeOrder();
        int index = order.indexOf(current);
        return index >= 0 && index < order.size() - 1 ? Optional.of(order.get(index + 1)) : Optional.empty();
    }

    public static Optional<Ear> nextEar(Ear current, TestState state) {
        var ears = state.config().ears();
        int index = ears.indexOf(current);
        return index >= 0 && index < ears.size() - 1 ? Optional.of(ears.get(index + 1)) : Optional.empty();
    }

    public static TonePresentation createTonePresentation(TestState state) {
        boolean ascending = state.direction() == PresentationDirection.ASCENDING
                || state.phase() == TestPhase.ASCENDING_SEARCH;
        return new TonePresentation(
                state.currentEar(),
                state.currentFrequency(),
                state.currentIntensity(),
                state.nextOrder(),
                Optional.empty(),
                state.direction(),
                ascending
        );
    }

    public static AudiogramPoint createAudiogramPoint(TestState state, IntensityDbHL threshold) {
        long count = state.presentations().stream()
                .filter(presentation -> presentation.ear() == state.currentEar())
                .filter(presentation -> presentation.frequency().equals(state.currentFrequency()))
                .count();
        return new AudiogramPoint(
                state.currentEar(),
                state.currentFrequency(),
                threshold,
                state.config().algorithm().criterion(),
                (int) count,
                state.presentations().size(),
                classifyHearingLevel(threshold)
        );
    }

    public static String classifyHearingLevel(IntensityDbHL threshold) {
        int value = threshold.value();
        if (value <= 25) return "Normal/near-normal (educational classification)";
        if (value <= 40) return "Mild";
        if (value <= 55) return "Moderate";
        if (value <= 70) return "Moderately severe";
        if (value <= 90) return "Severe";
        return "Profound";
    }
}
