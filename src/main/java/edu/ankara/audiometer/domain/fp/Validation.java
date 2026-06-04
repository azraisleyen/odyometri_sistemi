package edu.ankara.audiometer.domain.fp;

import java.util.List;
import java.util.Optional;

public record Validation<T>(Optional<T> value, List<String> errors) {
    public Validation {
        value = value == null ? Optional.empty() : value;
        errors = List.copyOf(errors);
    }

    public boolean valid() {
        return errors.isEmpty();
    }

    public static <T> Validation<T> ok(T value) {
        return new Validation<>(Optional.of(value), List.of());
    }

    public static <T> Validation<T> invalid(String error) {
        return new Validation<>(Optional.empty(), List.of(error));
    }
}
