package dev.hilla.parser.utils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public final class StreamUtils {
    private StreamUtils() {
    }

    @SafeVarargs
    public static <T> Stream<T> combine(Stream<T>... streams) {
        return Stream.of(streams).flatMap(Function.identity());
    }

    @SafeVarargs
    public static <T> Stream<T> combine(T[]... arrays) {
        return Stream.of(arrays).flatMap(Arrays::stream);
    }
}
