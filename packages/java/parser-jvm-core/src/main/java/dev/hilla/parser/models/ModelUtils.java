package dev.hilla.parser.models;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.github.classgraph.ClassInfo;

final class ModelUtils {
    public static boolean isJDKClass(Class<?> cls) {
        return isJDKClass(cls.getName());
    }

    public static boolean isJDKClass(ParameterizedType type) {
        return isJDKClass((Class<?>) type.getRawType());
    }

    public static boolean isJDKClass(Type type) {
        if (type instanceof ParameterizedType) {
            return isJDKClass((ParameterizedType) type);
        } else if (type instanceof Class<?>) {
            return isJDKClass((Class<?>) type);
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

    public static <T> boolean defaultClassInfoMemberFilter(T member) {
        return true;
    }
}
