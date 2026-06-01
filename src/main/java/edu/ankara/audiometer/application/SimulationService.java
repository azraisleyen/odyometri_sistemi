package edu.ankara.audiometer.application;

import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.TestState;

import java.util.HashMap;
import java.util.Map;

public final class SimulationService {
    private final Map<String, IntensityDbHL> thresholds = new HashMap<>();

    public void setThreshold(Ear ear, FrequencyHz frequency, IntensityDbHL threshold) {
        thresholds.put(key(ear, frequency), threshold);
    }

    public IntensityDbHL thresholdFor(Ear ear, FrequencyHz frequency) {
        return thresholds.getOrDefault(key(ear, frequency), new IntensityDbHL(ear == Ear.RIGHT ? 25 : 30));
    }

    public PatientSimulationDecision decide(TestState state) {
        var threshold = thresholdFor(state.currentEar(), state.currentFrequency());
        return new PatientSimulationDecision(state.currentIntensity().value() >= threshold.value(), threshold);
    }

    private static String key(Ear ear, FrequencyHz frequency) {
        return ear + ":" + frequency.value();
    }

    public record PatientSimulationDecision(boolean heard, IntensityDbHL threshold) {
        public String eventName() {
            return heard ? "RESPONSE" : "NO_RESPONSE";
        }
    }
}
