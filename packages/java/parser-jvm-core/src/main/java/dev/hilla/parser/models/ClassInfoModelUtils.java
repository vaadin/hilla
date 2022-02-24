package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Predicate;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

final class ClassInfoModelUtils {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };

    private ClassInfoModelUtils() {
    }

    public static <T> boolean defaultClassInfoMemberFilter(T member) {
        return true;
    }

    public static boolean is(Class<?> actor, Class<?> target) {
        return actor.equals(target);
    }

    public static boolean is(ClassInfo actor, Class<?> target) {
        return actor.getName().equals(target.getName());
    }

    public static boolean is(ClassRefTypeSignature actor, Class<?> target) {
        return actor.getFullyQualifiedClassName().equals(target.getName());
    }

    public static boolean isAssignableFrom(Class<?> target, Class<?> from) {
        return target.isAssignableFrom(from);
    }

    public static boolean isAssignableFrom(Class<?> target, ClassInfo from) {
        return is(from, target)
                || (target.isInterface() && from.implementsInterface(target))
                || from.extendsSuperclass(target);
    }

    public static boolean isDateAssignable(Predicate<Class<?>> predicate) {
        return Arrays.stream(DATE_CLASSES).anyMatch(predicate);
    }

    public static boolean isDateAssignable(Class<?> from) {
        return isDateAssignable(cls -> cls.isAssignableFrom(from));
    }

    public static boolean isDateAssignable(ClassInfo from) {
        return isDateAssignable(cls -> isAssignableFrom(cls, from));
    }

    public static boolean isDateTimeAssignable(Predicate<Class<?>> predicate) {
        return Arrays.stream(DATE_TIME_CLASSES).anyMatch(predicate);
    }

    public static boolean isDateTimeAssignable(Class<?> from) {
        return isDateTimeAssignable(cls -> cls.isAssignableFrom(from));
    }

    public static boolean isDateTimeAssignable(ClassInfo from) {
        return isDateTimeAssignable(cls -> isAssignableFrom(cls, from));
    }

    public static boolean isJDKClass(AnnotatedElement type) {
        if (type instanceof AnnotatedType) {
            return isJDKClass(((AnnotatedType) type).getType());
        } else if (type instanceof Class<?>) {
            return isJDKClass((Type) type);
        } else {
            return false;
        }
    }

    public static boolean isJDKClass(ClassInfo cls) {
        return isJDKClass(cls.getName());
    }

    public static boolean isJDKClass(String className) {
        return className.startsWith("java") || className.startsWith("com.sun")
                || className.startsWith("sun") || className.startsWith("oracle")
                || className.startsWith("org.xml")
                || className.startsWith("com.oracle");
    }

    public static boolean isJDKClass(Type cls) {
        return isJDKClass(cls.getTypeName());
    }
}
