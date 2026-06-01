package edu.ankara.audiometer.domain.model;

import java.util.List;

public enum EarTestMode {
    RIGHT_ONLY,
    LEFT_ONLY,
    BOTH;

    public List<Ear> ears() {
        return switch (this) {
            case RIGHT_ONLY -> List.of(Ear.RIGHT);
            case LEFT_ONLY -> List.of(Ear.LEFT);
            case BOTH -> List.of(Ear.RIGHT, Ear.LEFT);
        };
    }

    public static EarTestMode fromLabel(String label) {
        return switch (label) {
            case "Right" -> RIGHT_ONLY;
            case "Left" -> LEFT_ONLY;
            default -> BOTH;
        };
    }
}
