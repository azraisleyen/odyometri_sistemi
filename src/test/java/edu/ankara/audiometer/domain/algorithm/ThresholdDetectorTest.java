package edu.ankara.audiometer.domain.algorithm;

import edu.ankara.audiometer.domain.config.HughsonWestlakeConfig;
import edu.ankara.audiometer.domain.model.Ear;
import edu.ankara.audiometer.domain.model.FrequencyHz;
import edu.ankara.audiometer.domain.model.IntensityDbHL;
import edu.ankara.audiometer.domain.model.PatientResponse;
import edu.ankara.audiometer.domain.model.PresentationDirection;
import edu.ankara.audiometer.domain.model.ThresholdCriterion;
import edu.ankara.audiometer.domain.model.TonePresentation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThresholdDetectorTest {
    @Test
    void detectsTwoOutOfThreeAscendingInRecentWindow() {
        var list = List.of(
                presentation(1, 20, PatientResponse.HEARD),
                presentation(2, 20, PatientResponse.NOT_HEARD),
                presentation(3, 20, PatientResponse.HEARD)
        );
        var decision = ThresholdDetector.detectThreshold(list, HughsonWestlakeConfig.defaults());
        assertTrue(decision.reached());
        assertEquals(20, decision.threshold().orElseThrow().value());
    }

    @Test
    void detectsThreeOutOfFiveAscendingInRecentWindow() {
        var config = HughsonWestlakeConfig.defaults().withCriterion(ThresholdCriterion.THREE_OUT_OF_FIVE_ASCENDING);
        var list = List.of(
                presentation(1, 30, PatientResponse.HEARD),
                presentation(2, 30, PatientResponse.NOT_HEARD),
                presentation(3, 30, PatientResponse.HEARD),
                presentation(4, 30, PatientResponse.TIMEOUT),
                presentation(5, 30, PatientResponse.HEARD)
        );
        assertTrue(ThresholdDetector.detectThreshold(list, config).reached());
    }

    @Test
    void olderTrialsOutsideRecentWindowDoNotForceThreshold() {
        var list = List.of(
                presentation(1, 25, PatientResponse.HEARD),
                presentation(2, 25, PatientResponse.HEARD),
                presentation(3, 25, PatientResponse.NOT_HEARD),
                presentation(4, 25, PatientResponse.NOT_HEARD)
        );
        assertFalse(ThresholdDetector.detectThreshold(list, HughsonWestlakeConfig.defaults()).reached());
    }

    @Test
    void filtersCurrentEarAndFrequency() {
        var otherEar = new TonePresentation(
                Ear.LEFT,
                new FrequencyHz(1000),
                new IntensityDbHL(20),
                1,
                Optional.of(PatientResponse.HEARD),
                PresentationDirection.ASCENDING,
                true
        );
        var current = presentation(2, 20, PatientResponse.NOT_HEARD);
        assertFalse(ThresholdDetector.detectThreshold(List.of(otherEar, otherEar, current), HughsonWestlakeConfig.defaults()).reached());
    }

    @Test
    void ignoresDescendingTrials() {
        var descending = new TonePresentation(
                Ear.RIGHT,
                new FrequencyHz(1000),
                new IntensityDbHL(10),
                1,
                Optional.of(PatientResponse.HEARD),
                PresentationDirection.DESCENDING,
                false
        );
        assertFalse(ThresholdDetector.detectThreshold(List.of(descending, descending, descending), HughsonWestlakeConfig.defaults()).reached());
    }

    private static TonePresentation presentation(int order, int db, PatientResponse response) {
        return new TonePresentation(
                Ear.RIGHT,
                new FrequencyHz(1000),
                new IntensityDbHL(db),
                order,
                Optional.of(response),
                PresentationDirection.ASCENDING,
                true
        );
    }
}
