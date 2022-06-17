package dev.hilla.parser.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.function.BiPredicate;

import io.github.classgraph.ClassInfo;

final class ClassInfoModelUtils {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };

    private ClassInfoModelUtils() {
    }

    public static <T> boolean isDateAssignable(T actor,
            BiPredicate<Class<?>, T> predicate) {
        for (var cls : DATE_CLASSES) {
            if (predicate.test(cls, actor)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isDateAssignable(Class<?> actor) {
        return isDateAssignable(actor, ClassInfoModel::isAssignableFrom);
    }

    public static boolean isDateAssignable(ClassInfo actor) {
        return isDateAssignable(actor, ClassInfoModel::isAssignableFrom);
    }

    public static <T> boolean isDateTimeAssignable(T actor,
            BiPredicate<Class<?>, T> predicate) {
        for (var cls : DATE_TIME_CLASSES) {
            if (predicate.test(cls, actor)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isDateTimeAssignable(Class<?> actor) {
        return isDateTimeAssignable(actor, ClassInfoModel::isAssignableFrom);
    }

    public static boolean isDateTimeAssignable(ClassInfo actor) {
        return isDateTimeAssignable(actor, ClassInfoModel::isAssignableFrom);
    }

}
