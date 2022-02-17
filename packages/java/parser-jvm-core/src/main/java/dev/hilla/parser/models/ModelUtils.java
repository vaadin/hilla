package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import io.github.classgraph.ClassInfo;

final class ModelUtils {
    public static <T> boolean defaultClassInfoMemberFilter(T member) {
        return true;
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
