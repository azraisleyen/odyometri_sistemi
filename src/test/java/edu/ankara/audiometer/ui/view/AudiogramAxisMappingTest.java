package edu.ankara.audiometer.ui.view;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AudiogramAxisMappingTest {
    @Test
    void standardFrequenciesUseAudiogramOrderAndEqualPositions() {
        assertEquals(List.of(250, 500, 1000, 2000, 4000, 8000), AudiogramAxisMapping.standardFrequencies());
        assertEquals(0, AudiogramAxisMapping.positionForFrequency(250));
        assertEquals(1, AudiogramAxisMapping.positionForFrequency(500));
        assertEquals(2, AudiogramAxisMapping.positionForFrequency(1000));
        assertEquals(3, AudiogramAxisMapping.positionForFrequency(2000));
        assertEquals(4, AudiogramAxisMapping.positionForFrequency(4000));
        assertEquals(5, AudiogramAxisMapping.positionForFrequency(8000));
    }

    @Test
    void unknownFrequencyUsesRawFrequencyFallback() {
        assertEquals(750, AudiogramAxisMapping.positionForFrequency(750));
    }

    @Test
    void notationAndProcedureLabelsAreJavaFxIndependent() {
        assertEquals("RIGHT red O", AudiogramNotation.RIGHT_SERIES_NAME);
        assertEquals("LEFT blue X", AudiogramNotation.LEFT_SERIES_NAME);
        assertEquals("Procedure", AudiometerUiLabels.PROCEDURE_LABEL);
        assertEquals("Automatic Hughson-Westlake", AudiometerUiLabels.PROCEDURE_VALUE);
        assertFalse(AudiometerUiLabels.PROCEDURE_VALUE.toLowerCase().contains("manual"));
    }
}
