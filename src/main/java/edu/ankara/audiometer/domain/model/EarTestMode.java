package edu.ankara.audiometer.domain.model;

import java.util.List;

public enum EarTestMode {
    BOTH("Both", List.of(Ear.RIGHT, Ear.LEFT)),
    RIGHT_ONLY("Right", List.of(Ear.RIGHT)),
    LEFT_ONLY("Left", List.of(Ear.LEFT));

    private final String label;
    private final List<Ear> ears;

    EarTestMode(String label, List<Ear> ears) {
        this.label = label;
        this.ears = ears;
    }

    public List<Ear> ears() {
        return ears;
    }

    @Override
    public String toString() {
        return label;
    }
}
