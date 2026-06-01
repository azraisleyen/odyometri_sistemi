package edu.ankara.audiometer.domain.config;

import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;

import java.util.List;

public record AudiometryConfig(
        FrequencyHz minFrequency,
        FrequencyHz maxFrequency,
        IntensityDbHL minIntensity,
        IntensityDbHL maxIntensity,
        FrequencyPlan frequencyPlan,
        HughsonWestlakeConfig algorithm,
        SerialProtocolConfig serialProtocol,
        List<Ear> ears,
        boolean manualFrequencyOverride,
        boolean allowRetest
) {
    public AudiometryConfig {
        if (ears == null || ears.isEmpty()) {
            throw new IllegalArgumentException("at least one ear must be configured");
        }
        ears = List.copyOf(ears);
    }

    public static AudiometryConfig defaults() {
        return new AudiometryConfig(
                new FrequencyHz(250),
                new FrequencyHz(8000),
                new IntensityDbHL(-10),
                new IntensityDbHL(120),
                FrequencyPlan.defaults(),
                HughsonWestlakeConfig.defaults(),
                SerialProtocolConfig.defaults(),
                List.of(Ear.RIGHT, Ear.LEFT),
                false,
                false
        );
    }

    public AudiometryConfig withCriterion(ThresholdCriterion criterion) {
        return new AudiometryConfig(
                minFrequency,
                maxFrequency,
                minIntensity,
                maxIntensity,
                frequencyPlan,
                algorithm.withCriterion(criterion),
                serialProtocol,
                ears,
                manualFrequencyOverride,
                allowRetest
        );
    }

    public AudiometryConfig withEars(List<Ear> selectedEars) {
        return new AudiometryConfig(
                minFrequency,
                maxFrequency,
                minIntensity,
                maxIntensity,
                frequencyPlan,
                algorithm,
                serialProtocol,
                selectedEars,
                manualFrequencyOverride,
                allowRetest
        );
    }
}
