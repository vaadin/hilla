package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

public interface ClassRefSignatureModel extends SignatureModel {
    static boolean is(ClassRefTypeSignature actor, Class<?> target) {
        return Objects.equals(actor.getFullyQualifiedClassName(),
                target.getName());
    }

    static boolean is(ClassRefTypeSignature actor, ClassInfo target) {
        return Objects.equals(actor.getFullyQualifiedClassName(),
                target.getName());
    }

    static ClassRefSignatureModel of(@Nonnull ClassRefTypeSignature origin,
            @Nonnull Model parent) {
        return new ClassRefSignatureSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static ClassRefSignatureModel of(@Nonnull Class<?> origin) {
        return of(origin, null);
    }

    static ClassRefSignatureModel of(@Nonnull Class<?> origin, Model parent) {
        return new ClassRefSignatureReflectionModel.Bare(origin, parent);
    }

    static ClassRefSignatureModel of(
            @Nonnull AnnotatedParameterizedType origin) {
        return of(origin, null);
    }

    static ClassRefSignatureModel of(@Nonnull AnnotatedParameterizedType origin,
            Model parent) {
        return new ClassRefSignatureReflectionModel(origin, parent);
    }

    List<TypeArgumentModel> getTypeArguments();

    default Stream<TypeArgumentModel> getTypeArgumentsStream() {
        return getTypeArguments().stream();
    }

    @Override
    default boolean isBoolean() {
        return resolve().isBoolean();
    }

    @Override
    default boolean isByte() {
        return resolve().isByte();
    }

    @Override
    default boolean isCharacter() {
        return resolve().isCharacter();
    }

    @Override
    default boolean isClassRef() {
        return true;
    }

    @Override
    default boolean isDate() {
        return resolve().isDate();
    }

    @Override
    default boolean isDateTime() {
        return resolve().isDateTime();
    }

    @Override
    default boolean isDouble() {
        return resolve().isDouble();
    }

    @Override
    default boolean isEnum() {
        return resolve().isEnum();
    }

    @Override
    default boolean isFloat() {
        return resolve().isFloat();
    }

    @Override
    default boolean isInteger() {
        return resolve().isInteger();
    }

    @Override
    default boolean isIterable() {
        return resolve().isIterable();
    }

    @Override
    default boolean isJDKClass() {
        return resolve().isJDKClass();
    }

    @Override
    default boolean isLong() {
        return resolve().isLong();
    }

    @Override
    default boolean isMap() {
        return resolve().isMap();
    }

    @Override
    default boolean isNativeObject() {
        return resolve().isNativeObject();
    }

    @Override
    default boolean isOptional() {
        return resolve().isOptional();
    }

    @Override
    default boolean isShort() {
        return resolve().isShort();
    }

    @Override
    default boolean isString() {
        return resolve().isString();
    }

    ClassInfoModel resolve();

    void setReference(ClassInfoModel reference);
}
