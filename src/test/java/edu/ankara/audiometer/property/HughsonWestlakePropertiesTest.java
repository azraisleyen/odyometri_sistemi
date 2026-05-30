package edu.ankara.audiometer.property;

import edu.ankara.audiometer.domain.algorithm.AudiometryFunctions;
import edu.ankara.audiometer.domain.algorithm.HughsonWestlakeEngine;
import edu.ankara.audiometer.domain.algorithm.ThresholdDetector;
import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.PresentationDirection;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.TonePresentation;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HughsonWestlakePropertiesTest {
    private final AudiometryConfig config = AudiometryConfig.defaults();

    @Property
    void intensityAlwaysInsideRange(@ForAll @IntRange(min = 0, max = 40) int steps) {
        TestState state = TestState.initial(config);
        for (int i = 0; i < steps; i++) {
            state = HughsonWestlakeEngine.presentTone(state);
            state = HughsonWestlakeEngine.applyResponseToLastPresentation(state, i % 2 == 0 ? PatientResponse.HEARD : PatientResponse.NOT_HEARD);
            assertTrue(state.currentIntensity().value() >= config.minIntensity().value());
            assertTrue(state.currentIntensity().value() <= config.maxIntensity().value());
        }
    }

    @Property
    void heardResponseNeverIncreasesIntensity(@ForAll @IntRange(min = -10, max = 120) int db) {
        IntensityDbHL next = AudiometryFunctions.nextIntensityAfterResponse(new IntensityDbHL(db), PatientResponse.HEARD, config.algorithm(), config);
        assertTrue(next.value() <= db);
    }

    @Property
    void noResponseNeverDecreasesIntensity(@ForAll @IntRange(min = -10, max = 120) int db) {
        IntensityDbHL next = AudiometryFunctions.nextIntensityAfterResponse(new IntensityDbHL(db), PatientResponse.NOT_HEARD, config.algorithm(), config);
        assertTrue(next.value() >= db);
    }

    @Property
    void thresholdResultBelongsToTestedIntensity(@ForAll @IntRange(min = 10, max = 60) int db) {
        var presentations = List.of(presentation(db, 1), presentation(db, 2), presentation(db, 3));
        var decision = ThresholdDetector.detectThreshold(presentations, config.algorithm());
        decision.threshold().ifPresent(threshold -> assertEquals(db, threshold.value()));
    }

    private static TonePresentation presentation(int db, int order) {
        return new TonePresentation(Ear.LEFT, new FrequencyHz(2000), new IntensityDbHL(db), order, Optional.of(PatientResponse.HEARD), PresentationDirection.ASCENDING, true);
    }
}
