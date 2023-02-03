package dev.hilla.parser.models;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationParameterValue;

public abstract class AnnotationParameterModel implements Model, NamedModel {
    private Object value;

    public static AnnotationParameterModel of(@Nonnull String name,
            @Nonnull Object value, boolean isDefault) {
        return of(new ReflectionOrigin<>(Objects.requireNonNull(name),
                Objects.requireNonNull(value), isDefault));
    }

    public static <T> AnnotationParameterModel of(
            @Nonnull ReflectionOrigin<T> origin) {
        return new AnnotationParameterReflectionModel<>(
                Objects.requireNonNull(origin));
    }

    @Deprecated
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
    public Class<AnnotationParameterModel> getCommonModelClass() {
        return AnnotationParameterModel.class;
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

    public abstract boolean isDefault();

    @Override
    public String toString() {
        return "AnnotationParameterModel[" + get() + "]";
    }

    protected abstract Object prepareValue();

    public static final class ReflectionOrigin<T> {
        private final boolean isDefault;
        private final String name;
        private final T value;

        public ReflectionOrigin(String name, T value, boolean isDefault) {
            this.name = name;
            this.value = value;
            this.isDefault = isDefault;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ReflectionOrigin)) {
                return false;
            }

            var other = (ReflectionOrigin<?>) obj;

            return name.equals(other.name) && value.equals(other.value)
                    && isDefault == other.isDefault;
        }

        public String getName() {
            return name;
        }

        public T getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return (name.hashCode() + value.hashCode()
                    + Boolean.hashCode(isDefault)) ^ 0x10e6f7b;
        }

        public boolean isDefault() {
            return isDefault;
        }

        @Override
        public String toString() {
            return "ReflectionOrigin[name=" + name + ",value=" + value
                    + ",isDefault=" + isDefault + "]";
        }
    }
}
