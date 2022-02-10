package dev.hilla.parser.models;

import javax.annotation.Nonnull;

import java.util.Objects;

import io.github.classgraph.ClassInfo;

public interface ClassInfoModel {
    static ClassInfoModel of(@Nonnull ClassInfo origin) {
        return of(origin, null);
    }

    static ClassInfoModel of(@Nonnull ClassInfo origin, Dependable<?, ?> parent) {
        return new ClassInfoSourceModel(Objects.requireNonNull(origin), parent);
    }

    static ClassInfoModel of(@Nonnull Class<?> origin) {
        return of(origin, null);
    }

    static ClassInfoModel of(@Nonnull Class<?> origin, Dependable<?, ?> parent) {
        return new ClassInfoReflectionModel(Objects.requireNonNull(origin), parent);
    }
}
