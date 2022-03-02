package dev.hilla.parser.models;

final class BaseSignatureReflectionModel
        extends AbstractAnnotatedReflectionModel<Class<?>>
        implements BaseSignatureModel, ReflectionSignatureModel {
    public BaseSignatureReflectionModel(Class<?> origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public boolean isBoolean() {
        return origin == Boolean.TYPE;
    }

    @Override
    public boolean isByte() {
        return origin == Byte.TYPE;
    }

    @Override
    public boolean isCharacter() {
        return origin == Character.TYPE;
    }

    @Override
    public boolean isDouble() {
        return origin == Double.TYPE;
    }

    @Override
    public boolean isFloat() {
        return origin == Float.TYPE;
    }

    @Override
    public boolean isInteger() {
        return origin == Integer.TYPE;
    }

    @Override
    public boolean isJDKClass() {
        return true;
    }

    @Override
    public boolean isLong() {
        return origin == Long.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        return origin != Void.TYPE;
    }

    @Override
    public boolean isShort() {
        return origin == Short.TYPE;
    }

    @Override
    public boolean isVoid() {
        return origin == Void.TYPE;
    }
}
