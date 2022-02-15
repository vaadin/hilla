package dev.hilla.parser.models;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

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

    static ClassRefSignatureModel of(@Nonnull ParameterizedType origin) {
        return of(origin, null);
    }

    static ClassRefSignatureModel of(@Nonnull ParameterizedType origin,
            Model parent) {
        return new ClassRefSignatureReflectionModel(
                (Class<?>) origin.getRawType(), origin, parent);
    }

    static Stream<ClassInfo> resolveDependencies(
            @Nonnull ClassRefTypeSignature signature) {
        var classInfo = Objects.requireNonNull(signature).getClassInfo();

        var typeArgumentsDependencies = signature.getTypeArguments().stream()
                .flatMap(SignatureModel::resolveDependencies).distinct();

        return classInfo != null && !ModelUtils.isJDKClass(classInfo.getName())
                ? Stream.of(Stream.of(classInfo), typeArgumentsDependencies)
                        .flatMap(Function.identity()).distinct()
                : typeArgumentsDependencies;
    }

    static Stream<Type> resolveDependencies(@Nonnull Type signature) {
        var typeArgumentDependencies = Objects
                .requireNonNull(signature) instanceof ParameterizedType ? Arrays
                        .stream(((ParameterizedType) signature)
                                .getActualTypeArguments())
                        .flatMap(SignatureModel::resolveDependencies)
                        .distinct() : Stream.<Type> empty();

        return ModelUtils.isJDKClass(signature)
                ? Stream.of(Stream.of(signature), typeArgumentDependencies)
                        .flatMap(Function.identity()).distinct()
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
