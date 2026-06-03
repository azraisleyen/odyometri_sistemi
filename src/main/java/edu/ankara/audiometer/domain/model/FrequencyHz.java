package edu.ankara.audiometer.domain.model;

public record FrequencyHz(int value) implements Comparable<FrequencyHz> {
    public FrequencyHz {
        if (value <= 0) {
            throw new IllegalArgumentException("frequency must be positive");
        }
    }

    @Override
    public int compareTo(FrequencyHz other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return value + " Hz";
    }
}
