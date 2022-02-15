package dev.hilla.parser.models;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeArgument;

public interface TypeArgumentModel extends SignatureModel {
    static TypeArgumentModel of(@Nonnull TypeArgument origin,
            @Nonnull Model parent) {
        return new TypeArgumentSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static TypeArgumentModel of(@Nonnull Type origin, Model parent) {
        return new TypeArgumentReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    static Stream<ClassInfo> resolveDependencies(TypeArgument signature) {
        return SignatureModel
                .resolveDependencies(signature.getTypeSignature());
    }

    static Stream<Type> resolveDependencies(WildcardType signature) {
        return Stream
                .of(Arrays.stream(signature.getUpperBounds()),
                        Arrays.stream(signature.getLowerBounds()))
                .flatMap(Function.identity())
                .flatMap(SignatureModel::resolveDependencies);
    }

    Collection<SignatureModel> getAssociatedTypes();

    TypeArgument.Wildcard getWildcard();

    @Override
    default boolean isTypeArgument() {
        return true;
    }
}
