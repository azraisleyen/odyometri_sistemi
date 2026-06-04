package edu.ankara.audiometer.domain.fp;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public sealed interface Maybe<T> permits Maybe.Some, Maybe.None {
    boolean isPresent();

    T orElse(T other);

    <U> Maybe<U> map(Function<T, U> mapper);

    Optional<T> toOptional();

    static <T> Maybe<T> some(T value) {
        return new Some<>(Objects.requireNonNull(value));
    }

    static <T> Maybe<T> none() {
        return new None<>();
    }

    record Some<T>(T value) implements Maybe<T> {
        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T orElse(T other) {
            return value;
        }

        @Override
        public <U> Maybe<U> map(Function<T, U> mapper) {
            return some(mapper.apply(value));
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }
    }

    record None<T>() implements Maybe<T> {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public <U> Maybe<U> map(Function<T, U> mapper) {
            return none();
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }
    }
}
