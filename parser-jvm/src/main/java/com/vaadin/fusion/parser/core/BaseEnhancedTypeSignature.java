package com.vaadin.fusion.parser.core;

import java.util.Arrays;
import java.util.List;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.TypeSignature;

public final class BaseEnhancedTypeSignature extends EnhancedTypeSignature {
    public BaseEnhancedTypeSignature(TypeSignature signature) {
        super(signature);
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isBase() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        return ((BaseTypeSignature) signature).getType() == Boolean.TYPE;
    }

    @Override
    public boolean isClassRef() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isDate() {
        return false;
    }

    @Override
    public boolean isDateTime() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public boolean isNumber() {
        Class<?> type = ((BaseTypeSignature) signature).getType();

        return type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE
                || type == Long.TYPE || type == Float.TYPE
                || type == Double.TYPE;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public boolean isString() {
        return ((BaseTypeSignature) signature).getType() == Character.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        Class<?> type = ((BaseTypeSignature) signature).getType();

        return type != null && type != Void.TYPE;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public boolean isVoid() {
        return ((BaseTypeSignature) signature).getType() == Void.TYPE;
    }
}
