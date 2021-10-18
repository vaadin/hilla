package com.vaadin.fusion.parser.core;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;

public final class BaseRelativeTypeSignature
        extends AbstractRelative<BaseTypeSignature, Relative<?>>
        implements RelativeTypeSignature {
    BaseRelativeTypeSignature(BaseTypeSignature origin, Relative<?> parent) {
        super(origin, parent);
    }

    public static Stream<ClassInfo> resolve(@Nonnull BaseTypeSignature signature) {
        // BaseTypeSignature is about primitive types (int, double, etc.).
        // We don't need to resolve them, so skipping.
        return Stream.empty();
    }

    @Override
    public boolean isBase() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        return origin.getType() == Boolean.TYPE;
    }

    @Override
    public boolean isNumber() {
        Class<?> type = origin.getType();

        return type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE
                || type == Long.TYPE || type == Float.TYPE
                || type == Double.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        Class<?> type = origin.getType();

        return type != null && type != Void.TYPE;
    }

    @Override
    public boolean isString() {
        return origin.getType() == Character.TYPE;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public boolean isVoid() {
        return origin.getType() == Void.TYPE;
    }
}
