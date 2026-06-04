package edu.ankara.audiometer.ui.view;

import java.util.List;

public final class AudiogramAxisMapping {
    private static final List<Integer> STANDARD_FREQUENCIES = List.of(250, 500, 1000, 2000, 4000, 8000);

    private AudiogramAxisMapping() {
    }

    public static List<Integer> standardFrequencies() {
        return STANDARD_FREQUENCIES;
    }

    /**
     * Maps standard audiogram frequencies to equal visual positions. Unknown values retain the
     * previous fallback behavior by returning the raw frequency value.
     */
    public static int positionForFrequency(int frequencyHz) {
        int index = STANDARD_FREQUENCIES.indexOf(frequencyHz);
        return index >= 0 ? index : frequencyHz;
    }
}
