package dev.hilla.parser.models;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeArgument;

public interface TypeArgumentModel extends TypeModel {
    static TypeArgumentModel of(@Nonnull TypeArgument origin,
            @Nonnull Dependable<?, ?> parent) {
        return new TypeArgumentSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static TypeArgumentModel of(@Nonnull WildcardType origin,
            Dependable<?, ?> parent) {
        return new TypeArgumentReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    static Stream<ClassInfo> resolve(TypeArgument signature) {
        return SourceSignatureModel.resolve(signature.getTypeSignature());
    }

    static Stream<Class<?>> resolve(ParameterizedType signature) {
        return ReflectionSignatureModel
                .resolve(signature.getActualTypeArguments()[0]);
    }

    TypeArgument.Wildcard getWildcard();

    Collection<TypeModel> getWildcardAssociatedTypes();

    @Override
    default boolean isTypeArgument() {
        return true;
    }
}
