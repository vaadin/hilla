package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;

public final class BaseRelativeTypeSignature
        extends AbstractRelative<BaseTypeSignature, Relative<?>>
        implements RelativeTypeSignature {
    BaseRelativeTypeSignature(BaseTypeSignature origin, Relative<?> parent) {
        super(origin, parent);
    }

    public static Stream<ClassInfo> resolve(
            @Nonnull BaseTypeSignature signature) {
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
    public boolean isByte() {
        return origin.getType() == Byte.TYPE;
    }

    @Override
    public boolean isDouble() {
        return origin.getType() == Double.TYPE;
    }

    @Override
    public boolean isFloat() {
        return origin.getType() == Float.TYPE;
    }

    @Override
    public boolean isInteger() {
        return origin.getType() == Integer.TYPE;
    }

    @Override
    public boolean isLong() {
        return origin.getType() == Long.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        var type = origin.getType();

        return type != null && type != Void.TYPE;
    }

    @Override
    public boolean isShort() {
        return origin.getType() == Short.TYPE;
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
