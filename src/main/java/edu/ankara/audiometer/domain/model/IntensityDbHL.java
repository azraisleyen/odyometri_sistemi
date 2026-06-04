package edu.ankara.audiometer.domain.model;

public record IntensityDbHL(int value) implements Comparable<IntensityDbHL> {
    @Override
    public int compareTo(IntensityDbHL other) {
        return Integer.compare(value, other.value);
    }

    public IntensityDbHL plus(int step, int min, int max) {
        return new IntensityDbHL(Math.max(min, Math.min(max, value + step)));
    }

    @Override
    public String toString() {
        return value + " dB HL";
    }
}
