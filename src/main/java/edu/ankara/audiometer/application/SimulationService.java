package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.TestState;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class SimulationService {
    private static final int RIGHT_FALLBACK_THRESHOLD_DB_HL = 25;
    private static final int LEFT_FALLBACK_THRESHOLD_DB_HL = 30;

    private final Map<Ear, Map<Integer, IntensityDbHL>> thresholds;

    public SimulationService() {
        this(defaultDemoThresholds());
    }

    public SimulationService(Map<Ear, Map<Integer, IntensityDbHL>> thresholds) {
        this.thresholds = copyThresholds(thresholds);
    }

    public static Map<Ear, Map<Integer, IntensityDbHL>> defaultDemoThresholds() {
        Map<Ear, Map<Integer, IntensityDbHL>> profile = new EnumMap<>(Ear.class);
        profile.put(Ear.RIGHT, thresholdsByFrequency(Map.of(
                250, 20,
                500, 20,
                1000, 25,
                2000, 30,
                4000, 35,
                8000, 40
        )));
        profile.put(Ear.LEFT, thresholdsByFrequency(Map.of(
                250, 25,
                500, 30,
                1000, 30,
                2000, 35,
                4000, 40,
                8000, 45
        )));
        return profile;
    }

    public void setThreshold(Ear ear, FrequencyHz frequency, IntensityDbHL threshold) {
        thresholds.computeIfAbsent(ear, ignored -> new HashMap<>()).put(frequency.value(), threshold);
    }

    public IntensityDbHL thresholdFor(Ear ear, FrequencyHz frequency) {
        return thresholds.getOrDefault(ear, Map.of())
                .getOrDefault(frequency.value(), fallbackThresholdFor(ear));
    }

    public PatientSimulationDecision decide(TestState state) {
        var threshold = thresholdFor(state.currentEar(), state.currentFrequency());
        return new PatientSimulationDecision(state.currentIntensity().value() >= threshold.value(), threshold);
    }

    private static IntensityDbHL fallbackThresholdFor(Ear ear) {
        return new IntensityDbHL(ear == Ear.RIGHT ? RIGHT_FALLBACK_THRESHOLD_DB_HL : LEFT_FALLBACK_THRESHOLD_DB_HL);
    }

    private static Map<Integer, IntensityDbHL> thresholdsByFrequency(Map<Integer, Integer> values) {
        Map<Integer, IntensityDbHL> thresholdsByFrequency = new HashMap<>();
        values.forEach((frequency, threshold) -> thresholdsByFrequency.put(frequency, new IntensityDbHL(threshold)));
        return thresholdsByFrequency;
    }

    private static Map<Ear, Map<Integer, IntensityDbHL>> copyThresholds(Map<Ear, Map<Integer, IntensityDbHL>> source) {
        Map<Ear, Map<Integer, IntensityDbHL>> copied = new EnumMap<>(Ear.class);
        source.forEach((ear, byFrequency) -> copied.put(ear, new HashMap<>(byFrequency)));
        return copied;
    }

    public record PatientSimulationDecision(boolean heard, IntensityDbHL threshold) {
        public String eventName() {
            return heard ? "RESPONSE" : "NO_RESPONSE";
        }
    }
}
