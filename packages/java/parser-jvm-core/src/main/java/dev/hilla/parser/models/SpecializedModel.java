package dev.hilla.parser.models;

public interface SpecializedModel {
    default boolean hasFloatType() {
        return isFloat() || isDouble();
    }

    default boolean hasIntegerType() {
        return isByte() || isShort() || isInteger() || isLong();
    }

    default boolean isArray() {
        return false;
    }

    default boolean isBase() {
        return false;
    }

    default boolean isBoolean() {
        return false;
    }

    default boolean isByte() {
        return false;
    }

    default boolean isCharacter() {
        return false;
    }

    default boolean isClassRef() {
        return false;
    }

    default boolean isDate() {
        return false;
    }

    default boolean isDateTime() {
        return false;
    }

    default boolean isDouble() {
        return false;
    }

    default boolean isEnum() {
        return false;
    }

    default boolean isFloat() {
        return false;
    }

    default boolean isInteger() {
        return false;
    }

    default boolean isIterable() {
        return false;
    }

    default boolean isJDKClass() {
        return false;
    }

    default boolean isLong() {
        return false;
    }

    default boolean isMap() {
        return false;
    }

    default boolean isNativeObject() {
        return false;
    }

    default boolean isNonJDKClass() {
        return !isJDKClass();
    }

    default boolean isOptional() {
        return false;
    }

    default boolean isPrimitive() {
        return false;
    }

    default boolean isShort() {
        return false;
    }

    default boolean isString() {
        return false;
    }

    default boolean isTypeArgument() {
        return false;
    }

    default boolean isTypeParameter() {
        return false;
    }

    default boolean isTypeVariable() {
        return false;
    }

    default boolean isVoid() {
        return false;
    }
}
