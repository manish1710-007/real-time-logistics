package com.logistics.shared.domain;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface result<T> permits Result.Success, Result.Failure {
    boolean isSuccess();
    boolean isFailure();
    T getValue();
    String getError();

    default <R> Result<R> map(Function<T, R> mapper) {
        return switch (this) {
            case Success<T> s -> Result.success(mapper.apply(s.getValue()));
            case Failure<T> f -> Result.failure(f.getError());
        };
    }

    default <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        return switch (this) {
            case Success<T> s -> mapper.apply(s.getValue());
            case Failure<T> f -> Result.failure(f.getError());
        };
    }

    default Result<T> onSuccess(Consumer<T> action) {
        if (this instanceof Success<T> s) action.accept(s.value);
        return this;
    }

    default Result<T> onFailure(Consumer<String> action) {
        if (this instanceof Failure<T> f) action.accept(f.error);
        return this;
    }

    default T getOrElse(Supplier<T> fallback) {
        return switch (this) {
            case Success<T> s -> s.getValue();
            case Failure<T> f -> fallback.get();
        };
    }

    static <T> Result<T> success(T value) { return new Success<>(value); }
    static <T> Result<T> failure(String error) { return new Failure<>(error); }

    record Success<T>(T value) implements Result<T> {
        @Override public boolean isSuccess() { return true; }
        @Override public boolean isFailure() { return false; }
        @Override public T getValue() { return value; }
        @Override public String getError() { return null; }

    }

    record Failure<T>(String error) implements Result<T> {
        @Override public boolean isSuccess() { return false; }
        @Override public boolean isFailure() { return true; }
        @Override public T getValue() { return null; }
        @Override public String getError() { return error; }

    }
}