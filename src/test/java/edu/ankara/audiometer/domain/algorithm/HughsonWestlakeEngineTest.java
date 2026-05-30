package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.config.AudiometryConfig;
import edu.ankara.audiometer.domain.model.Audiogram;
import edu.ankara.audiometer.domain.model.AudiogramPoint;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.TestState;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class HughsonWestlakeEngineTest {
    private final AudiometryConfig config = AudiometryConfig.defaults();

    @Test
    void heardResponseDecreasesIntensityBy10Db() {
        TestState state = HughsonWestlakeEngine.presentTone(TestState.initial(config));
        TestState next = HughsonWestlakeEngine.applyResponseToLastPresentation(state, PatientResponse.HEARD);
        assertEquals(30, next.currentIntensity().value());
    }

    @Test
    void noResponseIncreasesIntensityBy5Db() {
        TestState state = HughsonWestlakeEngine.presentTone(TestState.initial(config));
        TestState next = HughsonWestlakeEngine.applyResponseToLastPresentation(state, PatientResponse.NOT_HEARD);
        assertEquals(45, next.currentIntensity().value());
    }

    @Test
    void rightAndLeftResultsAreIndependent() {
        AudiogramPoint right = new AudiogramPoint(Ear.RIGHT, new FrequencyHz(1000), new IntensityDbHL(20), ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING, 3, 3, "right");
        AudiogramPoint left = new AudiogramPoint(Ear.LEFT, new FrequencyHz(1000), new IntensityDbHL(35), ThresholdCriterion.TWO_OUT_OF_THREE_ASCENDING, 3, 6, "left");
        Audiogram audiogram = Audiogram.empty().add(right, false).add(left, false);

        assertEquals(2, audiogram.points().size());
        assertEquals(20, audiogram.find(Ear.RIGHT, new FrequencyHz(1000)).orElseThrow().thresholdDbHL().value());
        assertEquals(35, audiogram.find(Ear.LEFT, new FrequencyHz(1000)).orElseThrow().thresholdDbHL().value());
    }

    @Test
    void audiogramPointCreationUsesCurrentEarAndFrequency() {
        TestState state = TestState.initial(config);
        AudiogramPoint point = AudiometryFunctions.createAudiogramPoint(state, new IntensityDbHL(25));
        assertEquals(Ear.RIGHT, point.ear());
        assertEquals(1000, point.frequency().value());
    }

    @Test
    void stateUpdateIsImmutable() {
        TestState initial = TestState.initial(config);
        TestState presented = HughsonWestlakeEngine.presentTone(initial);
        assertNotSame(initial, presented);
        assertEquals(0, initial.presentations().size());
        assertEquals(1, presented.presentations().size());
    }
}
