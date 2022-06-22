package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

public interface ClassRefSignatureModel
        extends SignatureModel, OwnedModel<Optional<ClassRefSignatureModel>> {
    static boolean is(AnnotatedParameterizedType actor, Class<?> target) {
        return is(actor, target.getName());
    }

    static boolean is(AnnotatedParameterizedType actor, ClassInfo target) {
        return is(actor, target.getName());
    }

    static boolean is(AnnotatedParameterizedType actor, String target) {
        return ((Class<?>) ((ParameterizedType) actor.getType()).getRawType())
                .getName().equals(target);
    }

    static boolean is(AnnotatedType actor, Class<?> target) {
        return is(actor, target.getName());
    }

    static boolean is(AnnotatedType actor, ClassInfo target) {
        return is(actor, target.getName());
    }

    static boolean is(AnnotatedType actor, String target) {
        return actor instanceof AnnotatedParameterizedType
                ? is((AnnotatedParameterizedType) actor, target)
                : ((Class<?>) actor.getType()).getName().equals(target);
    }

    static boolean is(Class<?> actor, Class<?> target) {
        return actor.equals(target);
    }

    static boolean is(Class<?> actor, ClassInfo target) {
        return is(actor, target.getName());
    }

    static boolean is(Class<?> actor, String target) {
        return actor.getName().equals(target);
    }

    static boolean is(ClassRefTypeSignature actor, Class<?> target) {
        return is(actor, target.getName());
    }

    static boolean is(ClassRefTypeSignature actor, ClassInfo target) {
        return is(actor, target.getName());
    }

    static boolean is(ClassRefTypeSignature actor, String target) {
        return actor.getFullyQualifiedClassName().equals(target);
    }

    static ClassRefSignatureModel of(@Nonnull ClassRefTypeSignature origin) {
        return Objects.requireNonNull(origin).getSuffixes().size() > 0
                ? new ClassRefSignatureSourceModel.Suffixed(origin)
                : new ClassRefSignatureSourceModel.Regular(origin);
    }

    static ClassRefSignatureModel of(@Nonnull Class<?> origin) {
        return new ClassRefSignatureReflectionModel.Bare(origin);
    }

    static ClassRefSignatureModel of(@Nonnull AnnotatedType origin) {
        return ClassRefSignatureReflectionModel.Annotated.of(origin);
    }

    String getClassName();

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
