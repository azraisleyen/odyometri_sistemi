package edu.ankara.audiometer.domain.validation;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.fp.Validation;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.TestState;

import java.util.HashSet;

public final class AudiometryValidators {
    private AudiometryValidators() {
    }

    public static Validation<FrequencyHz> validateFrequency(FrequencyHz frequency, AudiometryConfig config) {
        if (frequency.value() < config.minFrequency().value() || frequency.value() > config.maxFrequency().value()) {
            return Validation.invalid("Frequency outside configured 250-8000 Hz range: " + frequency.value());
        }

        boolean configuredFrequency = config.frequencyPlan().requiredFrequencies().stream().anyMatch(frequency::equals)
                || config.frequencyPlan().optionalInterOctaves().stream().anyMatch(frequency::equals);
        if (!config.manualFrequencyOverride() && !configuredFrequency) {
            return Validation.invalid("Frequency not in configured plan: " + frequency.value());
        }

        return Validation.ok(frequency);
    }

    public static Validation<IntensityDbHL> validateIntensity(IntensityDbHL intensity, AudiometryConfig config) {
        if (intensity.value() < config.minIntensity().value() || intensity.value() > config.maxIntensity().value()) {
            return Validation.invalid("Intensity outside configured range: " + intensity.value());
        }
        return Validation.ok(intensity);
    }

    public static Validation<TestState> validateSessionCompleteness(TestState state) {
        var missing = state.config().ears().stream()
                .flatMap(ear -> state.config().frequencyPlan().requiredFrequencies().stream()
                        .map(frequency -> ear + ":" + frequency.value())
                        .filter(key -> state.audiogram().points().stream()
                                .noneMatch(point -> (point.ear() + ":" + point.frequency().value()).equals(key))))
                .toList();
        return missing.isEmpty()
                ? Validation.ok(state)
                : Validation.invalid("Missing thresholds: " + missing);
    }

    public static boolean hasDuplicateThresholds(TestState state) {
        var seen = new HashSet<String>();
        return state.audiogram().points().stream()
                .map(point -> point.ear() + ":" + point.frequency().value())
                .anyMatch(key -> !seen.add(key));
    }
}
