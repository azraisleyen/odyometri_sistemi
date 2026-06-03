package edu.ankara.audiometer.ui.view;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AudiogramChartPaneTest {
    @Test
    void standardFrequenciesUseAudiogramOrderAndEqualPositions() {
        assertEquals(List.of(250, 500, 1000, 2000, 4000, 8000), AudiogramChartPane.standardFrequencies());
        assertEquals(0, AudiogramChartPane.positionForFrequency(250));
        assertEquals(1, AudiogramChartPane.positionForFrequency(500));
        assertEquals(2, AudiogramChartPane.positionForFrequency(1000));
        assertEquals(3, AudiogramChartPane.positionForFrequency(2000));
        assertEquals(4, AudiogramChartPane.positionForFrequency(4000));
        assertEquals(5, AudiogramChartPane.positionForFrequency(8000));
    }

    @Test
    void seriesNamesMatchCustomAudiogramLegend() {
        assertEquals("RIGHT red O", AudiogramChartPane.RIGHT_SERIES_NAME);
        assertEquals("LEFT blue X", AudiogramChartPane.LEFT_SERIES_NAME);
    }

    @Test
    void manualModeIsNotExposedAsVisibleProcedureText() {
        assertEquals("Procedure: Automatic Hughson-Westlake", ControlPanel.PROCEDURE_LABEL);
        assertFalse(ControlPanel.PROCEDURE_LABEL.toLowerCase().contains("manual"));
    }
}
