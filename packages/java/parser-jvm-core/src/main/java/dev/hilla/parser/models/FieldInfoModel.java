package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.FieldInfo;

public interface FieldInfoModel extends Model, Dependable {
    static FieldInfoModel of(@Nonnull FieldInfo field, @Nonnull Model parent) {
        return new FieldInfoSourceModel(Objects.requireNonNull(field),
                Objects.requireNonNull(parent));
    }

    @Override
    default Collection<ClassInfoModel> getDependencies() {
        return getType().getDependencies();
    }

    SignatureModel getType();
}
