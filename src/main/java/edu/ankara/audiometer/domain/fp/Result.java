package edu.ankara.audiometer.domain.fp;

import java.util.function.Function;

public sealed interface Result<T, E> permits Result.Ok, Result.Err {
    boolean isOk();

    T orElse(T other);

    <U> Result<U, E> map(Function<T, U> mapper);

    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    record Ok<T, E>(T value) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public T orElse(T other) {
            return value;
        }

        @Override
        public <U> Result<U, E> map(Function<T, U> mapper) {
            return ok(mapper.apply(value));
        }
    }

    record Err<T, E>(E error) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public <U> Result<U, E> map(Function<T, U> mapper) {
            return err(error);
        }
    }
}
