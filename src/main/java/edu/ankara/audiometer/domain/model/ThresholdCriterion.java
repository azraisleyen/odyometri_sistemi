package edu.ankara.audiometer.domain.model;

public enum ThresholdCriterion {
    TWO_OUT_OF_THREE_ASCENDING(2, 3),
    THREE_OUT_OF_FIVE_ASCENDING(3, 5);

    public final int required;
    public final int window;

    ThresholdCriterion(int required, int window) {
        this.required = required;
        this.window = window;
    }
}
