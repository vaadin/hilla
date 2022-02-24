package dev.hilla.parser.models;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.FieldInfo;

public interface FieldInfoModel extends Model, NamedModel, AnnotatedModel {
    static FieldInfoModel of(@Nonnull FieldInfo field, @Nonnull Model parent) {
        return new FieldInfoSourceModel(Objects.requireNonNull(field),
                Objects.requireNonNull(parent));
    }

    static FieldInfoModel of(@Nonnull Field field, @Nonnull Model parent) {
        return new FieldInfoReflectionModel(field, parent);
    }

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return getType().getDependenciesStream();
    }

    SignatureModel getType();

    boolean isEnum();

    boolean isFinal();

    boolean isPrivate();

    boolean isProtected();

    boolean isPublic();

    boolean isStatic();

    boolean isSynthetic();

    boolean isTransient();
}
