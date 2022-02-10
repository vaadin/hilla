package dev.hilla.parser.models;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;

final class BaseSignatureSourceModel extends
        AbstractSourceSignatureDependable<BaseTypeSignature, Dependable<?, ?>>
        implements BaseSignatureModel, SourceSignatureModel {

    public BaseSignatureSourceModel(BaseTypeSignature origin,
            Dependable<?, ?> parent) {
        super(origin, parent);
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
    public boolean isCharacter() {
        return origin.getType() == Character.TYPE;
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
    public boolean isJDKClass() {
        return true;
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
    public boolean isVoid() {
        return origin.getType() == Void.TYPE;
    }
}
