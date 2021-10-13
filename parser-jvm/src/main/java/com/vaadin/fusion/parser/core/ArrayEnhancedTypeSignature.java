package com.vaadin.fusion.parser.core;

import io.github.classgraph.TypeSignature;

public final class ArrayEnhancedTypeSignature extends EnhancedTypeSignature {
    public ArrayEnhancedTypeSignature(TypeSignature signature) {
        super(signature);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isBase() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
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
        return false;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }
}
