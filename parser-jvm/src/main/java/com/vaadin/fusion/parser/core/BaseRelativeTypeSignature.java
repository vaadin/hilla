package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeSignature;

public final class BaseRelativeTypeSignature implements RelativeTypeSignature {
    public static Stream<ClassInfo> resolve(BaseTypeSignature signature) {
        // BaseTypeSignature is about primitive types (int, double, etc.).
        // We don't need to resolve them, so skipping.
        return Stream.empty();
    }

    private final BaseTypeSignature signature;

    public BaseRelativeTypeSignature(BaseTypeSignature signature) {
        this.signature = signature;
    }

    @Override
    public TypeSignature get() {
        return signature;
    }

    @Override
    public boolean isBase() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        return signature.getType() == Boolean.TYPE;
    }

    @Override
    public boolean isNumber() {
        Class<?> type = signature.getType();

        return type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE
                || type == Long.TYPE || type == Float.TYPE
                || type == Double.TYPE;
    }

    @Override
    public boolean isString() {
        return signature.getType() == Character.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        Class<?> type = signature.getType();

        return type != null && type != Void.TYPE;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public boolean isVoid() {
        return signature.getType() == Void.TYPE;
    }
}
