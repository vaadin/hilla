package com.vaadin.hilla.parser.test.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.hilla.parser.models.MethodInfoModel;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeSignature;

public final class ClassMemberUtils {
    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static Stream<MethodInfoModel> cleanup(
            Stream<MethodInfoModel> models) {
        return models.filter(ClassMemberUtils::skipJacoco);
    }

    public static Optional<Method> getAnyDeclaredMethod(Class<?> cls,
            String name) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> name.equals(m.getName())).findFirst();
    }

    public static ClassInfo getClassInfo(Class<?> cls, ScanResult source) {
        return source.getClassInfo(cls.getName());
    }

    public static Stream<Class<?>> getDeclaredClasses(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredClasses());
    }

    public static Stream<ClassInfo> getDeclaredClasses(Class<?> cls,
            ScanResult source) {
        return getClassInfo(cls, source).getInnerClasses().stream();
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> cls) {
        return getDeclaredConstructor(cls, List.of());
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> cls,
            List<Class<?>> parameterTypes) {
        try {
            return cls.getDeclaredConstructor(
                    parameterTypes.toArray(Class<?>[]::new));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodInfo getDeclaredConstructor(Class<?> cls,
            ScanResult source) {
        return getDeclaredConstructor(cls, List.of(), source);
    }

    public static MethodInfo getDeclaredConstructor(Class<?> cls,
            List<Class<?>> parameterTypes, ScanResult source) {
        return getDeclaredMethod(cls, "<init>", parameterTypes, source);
    }

    public static Stream<Constructor<?>> getDeclaredConstructors(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredConstructors());
    }

    public static Stream<MethodInfo> getDeclaredConstructors(Class<?> cls,
            ScanResult source) {
        return getClassInfo(cls, source).getDeclaredConstructorInfo().stream();
    }

    public static Field getDeclaredField(Class<?> cls, String name) {
        try {
            return cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static FieldInfo getDeclaredField(Class<?> cls, String name,
            ScanResult source) {
        return getClassInfo(cls, source).getDeclaredFieldInfo(name);
    }

    public static FieldInfo getDeclaredField(Field field, ScanResult source) {
        return getDeclaredField(field.getDeclaringClass(), field.getName(),
                source);
    }

    public static Stream<Field> getDeclaredFields(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredFields())
                .filter(ClassMemberUtils::skipJacoco);
    }

    public static Stream<FieldInfo> getDeclaredFields(Class<?> cls,
            ScanResult source) {
        return getClassInfo(cls, source).getDeclaredFieldInfo().stream();
    }

    public static Method getDeclaredMethod(Class<?> cls, String name) {
        return getDeclaredMethod(cls, name, List.of());
    }

    public static Method getDeclaredMethod(Class<?> cls, String name,
            List<Class<?>> parameterTypes) {
        try {
            return cls.getDeclaredMethod(name,
                    parameterTypes.toArray(Class<?>[]::new));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodInfo getDeclaredMethod(Class<?> cls, String name,
            ScanResult source) {
        return getDeclaredMethod(cls, name, List.of(), source);
    }

    public static MethodInfo getDeclaredMethod(Class<?> cls, String name,
            List<Class<?>> parameterTypes, ScanResult source) {
        var methods = getClassInfo(cls, source).getDeclaredMethodInfo(name);

        if (parameterTypes.isEmpty()) {
            return methods.getSingleMethod(name);
        }

        for (var method : methods) {
            var params = method.getParameterInfo();

            if (IntStream.range(0, params.length)
                    .allMatch(i -> areParameterTypesEqual(
                            params[i].getTypeSignatureOrTypeDescriptor(),
                            parameterTypes.get(i)))) {
                return method;
            }
        }

        throw new NoSuchElementException(
                "No method with specified parameter types found");
    }

    public static MethodInfo getDeclaredMethod(Executable method,
            ScanResult source) {
        return getDeclaredMethod(method.getDeclaringClass(),
                method instanceof Constructor ? "<init>" : method.getName(),
                List.of(method.getParameterTypes()), source);
    }

    public static Stream<Method> getDeclaredMethods(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(ClassMemberUtils::skipJacoco);
    }

    public static Stream<MethodInfo> getDeclaredMethods(Class<?> cls,
            ScanResult source) {
        return getClassInfo(cls, source).getDeclaredMethodInfo().stream();
    }

    public static Parameter getParameter(Executable method, int index) {
        return method.getParameters()[index];
    }

    public static MethodParameterInfo getParameter(MethodInfo method,
            int index) {
        return method.getParameterInfo()[index];
    }

    public static MethodParameterInfo getParameter(Parameter parameter,
            ScanResult source) {
        var method = parameter.getDeclaringExecutable();

        return Arrays
                .stream(getDeclaredMethod(method, source).getParameterInfo())
                .filter(param -> param.getName().equals(parameter.getName()))
                .findFirst().orElseThrow();
    }

    public static String toGetterName(String name) {
        return "get" + capitalize(name);
    }

    public static String toSetterName(String name) {
        return "set" + capitalize(name);
    }

    private static boolean areParameterTypesEqual(TypeSignature type,
            Class<?> expectedType) {
        return (type instanceof ClassRefTypeSignature
                && ((ClassRefTypeSignature) type).getFullyQualifiedClassName()
                        .equals(expectedType.getName()))
                || (type instanceof BaseTypeSignature
                        && ((BaseTypeSignature) type).getType()
                                .equals(expectedType))
                || (type instanceof ArrayTypeSignature && expectedType.isArray()
                        && areParameterTypesEqual(
                                ((ArrayTypeSignature) type).getNestedType(),
                                expectedType.getComponentType()));
    }

    private static boolean skipJacoco(Member member) {
        return !member.getName().contains("jacoco");
    }

    private static boolean skipJacoco(MethodInfoModel model) {
        return !model.getName().contains("jacoco");
    }
}
