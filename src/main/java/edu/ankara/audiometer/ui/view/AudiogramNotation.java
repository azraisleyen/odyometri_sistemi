package edu.ankara.audiometer.ui.view;

import edu.ankara.audiometer.domain.model.Ear;

public final class AudiogramNotation {
    public static final String RIGHT_SERIES_NAME = "RIGHT red O";
    public static final String LEFT_SERIES_NAME = "LEFT blue X";
    public static final String RIGHT_COLOR = "#dc2626";
    public static final String LEFT_COLOR = "#2563eb";

    private AudiogramNotation() {
    }

    public static String seriesName(Ear ear) {
        return ear == Ear.RIGHT ? RIGHT_SERIES_NAME : LEFT_SERIES_NAME;
    }

    public static String color(Ear ear) {
        return ear == Ear.RIGHT ? RIGHT_COLOR : LEFT_COLOR;
    }
}
