package dev.hilla.parser.utils;

import java.util.function.Predicate;

public final class Predicates {
    @SafeVarargs
    public static <T> Predicate<T> and(Predicate<T> predicate,
            Predicate<? super T>... predicates) {
        for (var p : predicates) {
            predicate = predicate.and(p);
        }

        return predicate;
    }
}
