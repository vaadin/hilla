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

    ClassInfoModel getClassInfo();

    String getClassName();

    List<TypeArgumentModel> getTypeArguments();

    default Stream<TypeArgumentModel> getTypeArgumentsStream() {
        return getTypeArguments().stream();
    }

    @Override
    default boolean isBoolean() {
        return getClassInfo().isBoolean();
    }

    @Override
    default boolean isByte() {
        return getClassInfo().isByte();
    }

    @Override
    default boolean isCharacter() {
        return getClassInfo().isCharacter();
    }

    @Override
    default boolean isClassRef() {
        return true;
    }

    @Override
    default boolean isDate() {
        return getClassInfo().isDate();
    }

    @Override
    default boolean isDateTime() {
        return getClassInfo().isDateTime();
    }

    @Override
    default boolean isDouble() {
        return getClassInfo().isDouble();
    }

    @Override
    default boolean isEnum() {
        return getClassInfo().isEnum();
    }

    @Override
    default boolean isFloat() {
        return getClassInfo().isFloat();
    }

    @Override
    default boolean isInteger() {
        return getClassInfo().isInteger();
    }

    @Override
    default boolean isIterable() {
        return getClassInfo().isIterable();
    }

    @Override
    default boolean isJDKClass() {
        return getClassInfo().isJDKClass();
    }

    @Override
    default boolean isLong() {
        return getClassInfo().isLong();
    }

    @Override
    default boolean isMap() {
        return getClassInfo().isMap();
    }

    @Override
    default boolean isNativeObject() {
        return getClassInfo().isNativeObject();
    }

    @Override
    default boolean isOptional() {
        return getClassInfo().isOptional();
    }

    @Override
    default boolean isShort() {
        return getClassInfo().isShort();
    }

    @Override
    default boolean isString() {
        return getClassInfo().isString();
    }

    void setReference(ClassInfoModel reference);
}
