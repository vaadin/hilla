package dev.hilla.parser.models;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationParameterValue;

public abstract class AnnotationParameterModel implements Model, NamedModel {
    private Object value;

    public static AnnotationParameterModel of(@Nonnull String name,
            @Nonnull Object value) {
        return of(Map.entry(Objects.requireNonNull(name),
                Objects.requireNonNull(value)));
    }

    public static <T> AnnotationParameterModel of(
            @Nonnull Map.Entry<String, T> origin) {
        return new AnnotationParameterReflectionModel<>(
                Objects.requireNonNull(origin));
    }

    public static AnnotationParameterModel of(
            @Nonnull AnnotationParameterValue origin) {
        return new AnnotationParameterSourceModel(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationParameterModel)) {
            return false;
        }

        var other = (AnnotationParameterModel) obj;

        return getName().equals(other.getName())
                && getValue().equals(other.getValue());
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        var value = getValue();

        if (value instanceof ClassInfoModel) {
            return Stream.of((ClassInfoModel) value);
        } else if (value instanceof AnnotationParameterEnumValueModel) {
            return Stream.of(
                    ((AnnotationParameterEnumValueModel) value).getClassInfo());
        }

        return Stream.empty();
    }

    public Object getValue() {
        if (value == null) {
            value = prepareValue();
        }

        return value;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 7 * getValue().hashCode();
    }

    protected abstract Object prepareValue();
}
