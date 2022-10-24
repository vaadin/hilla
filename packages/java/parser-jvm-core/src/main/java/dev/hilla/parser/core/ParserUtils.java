package dev.hilla.parser.core;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.NamedModel;
import io.github.classgraph.ClassInfo;

final class ParserUtils {
    public static boolean isJDKClass(Class<?> cls) {
        return isJDKClass(cls.getName());
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
}
