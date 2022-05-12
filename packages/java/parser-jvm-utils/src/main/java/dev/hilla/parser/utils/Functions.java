package dev.hilla.parser.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Functions {
    @FunctionalInterface
    public interface ThrowableFunction<T, R> {
        R apply(T value) throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowableConsumer<T> {
        void accept(T t) throws Throwable;
    }

    public static <T, R> Function<T, R> function(ThrowableFunction<T, R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Consumer<T> consumer(ThrowableConsumer<T> fn) {
        return t -> {
            try {
                fn.accept(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
