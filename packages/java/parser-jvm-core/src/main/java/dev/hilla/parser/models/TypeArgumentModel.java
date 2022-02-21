package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeArgument;

public interface TypeArgumentModel extends SignatureModel {
    static TypeArgumentModel of(@Nonnull TypeArgument origin,
            @Nonnull Model parent) {
        return new TypeArgumentSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static TypeArgumentModel of(@Nonnull AnnotatedType origin, Model parent) {
        return new TypeArgumentReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    static Stream<ClassInfo> resolveDependencies(TypeArgument signature) {
        return SignatureModel.resolveDependencies(signature.getTypeSignature());
    }

    static Stream<Class<?>> resolveDependencies(
            AnnotatedWildcardType signature) {
        return StreamUtils
                .combine(signature.getAnnotatedUpperBounds(),
                        signature.getAnnotatedLowerBounds())
                .flatMap(SignatureModel::resolveDependencies);
    }

    List<SignatureModel> getAssociatedTypes();

    TypeArgument.Wildcard getWildcard();

    @Override
    default boolean isTypeArgument() {
        return true;
    }
}
