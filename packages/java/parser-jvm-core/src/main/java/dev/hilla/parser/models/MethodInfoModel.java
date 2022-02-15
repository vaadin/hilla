package dev.hilla.parser.models;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodInfo;

public interface MethodInfoModel extends Model, Dependable {
    static MethodInfoModel of(@Nonnull MethodInfo method,
            @Nonnull Model parent) {
        return new MethodInfoSourceModel(Objects.requireNonNull(method),
                Objects.requireNonNull(parent));
    }
}
