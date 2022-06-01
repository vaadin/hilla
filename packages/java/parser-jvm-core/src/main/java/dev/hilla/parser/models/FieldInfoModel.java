package dev.hilla.parser.models;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.FieldInfo;

public interface FieldInfoModel
        extends Model, NamedModel, AnnotatedModel, OwnedModel<ClassInfoModel> {
    static FieldInfoModel of(@Nonnull FieldInfo field) {
        return new FieldInfoSourceModel(Objects.requireNonNull(field));
    }

    static FieldInfoModel of(@Nonnull Field field) {
        return new FieldInfoReflectionModel(field);
    }

    String getClassName();

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
