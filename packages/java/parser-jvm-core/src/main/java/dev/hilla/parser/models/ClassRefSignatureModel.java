package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

public interface ClassRefSignatureModel extends SignatureModel {
    static ClassRefSignatureModel of(@Nonnull ClassRefTypeSignature origin,
            @Nonnull Model parent) {
        return new ClassRefSignatureSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static ClassRefSignatureModel of(@Nonnull Class<?> origin) {
        return of(origin, null);
    }

    static ClassRefSignatureModel of(@Nonnull Class<?> origin, Model parent) {
        return new ClassRefSignatureReflectionModel(origin, parent);
    }

    static ClassRefSignatureModel of(
            @Nonnull AnnotatedParameterizedType origin) {
        return of(origin, null);
    }

    static ClassRefSignatureModel of(@Nonnull AnnotatedParameterizedType origin,
            Model parent) {
        return new ClassRefSignatureReflectionModel(origin, parent);
    }

    static Stream<ClassInfo> resolveDependencies(
            @Nonnull ClassRefTypeSignature signature) {
        var classInfo = Objects.requireNonNull(signature).getClassInfo();

        var typeArgumentsDependencies = signature.getTypeArguments().stream()
                .flatMap(SignatureModel::resolveDependencies).distinct();

        return classInfo != null && !ModelUtils.isJDKClass(classInfo.getName())
                ? StreamUtils.combine(Stream.of(classInfo),
                        typeArgumentsDependencies).distinct()
                : typeArgumentsDependencies;
    }

    static Stream<Class<?>> resolveDependencies(
            @Nonnull AnnotatedElement signature) {
        var typeArgumentDependencies = Objects
                .requireNonNull(signature) instanceof AnnotatedParameterizedType
                        ? Arrays.stream(((AnnotatedParameterizedType) signature)
                                .getAnnotatedActualTypeArguments())
                                .flatMap(SignatureModel::resolveDependencies)
                                .distinct()
                        : Stream.<Class<?>> empty();

        return signature instanceof Class<?>
                && !ModelUtils.isJDKClass(signature)
                        ? StreamUtils.combine(Stream.of((Class<?>) signature),
                                typeArgumentDependencies).distinct()
                        : typeArgumentDependencies;
    }

    Collection<TypeArgumentModel> getTypeArguments();

    default Stream<TypeArgumentModel> getTypeArgumentsStream() {
        return getTypeArguments().stream();
    }

    @Override
    default boolean isClassRef() {
        return true;
    }
}
