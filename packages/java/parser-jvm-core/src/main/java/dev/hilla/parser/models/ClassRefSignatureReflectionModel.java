package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

abstract class ClassRefSignatureReflectionModel<T extends AnnotatedElement>
        extends AbstractAnnotatedReflectionModel<T>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    public ClassRefSignatureReflectionModel(T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ClassRefSignatureModel)) {
            return false;
        }

        var other = (ClassRefSignatureModel) obj;

        return getClassName().equals(other.getClassName())
                && getTypeArguments().equals(other.getTypeArguments())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public int hashCode() {
        return getClassName().hashCode() + 7 * getTypeArguments().hashCode()
                + 13 * getAnnotations().hashCode();
    }

    static final class Regular extends
            ClassRefSignatureReflectionModel<AnnotatedParameterizedType> {
        private ClassInfoModel reference;
        private List<TypeArgumentModel> typeArguments;

        public Regular(@Nonnull AnnotatedParameterizedType origin) {
            super(origin);
        }

        @Override
        public String getClassName() {
            return resolveRaw().getName();
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            var owner = origin.getAnnotatedOwnerType();

            if (owner == null) {
                return Optional.empty();
            }

            return Optional.of(ClassRefSignatureModel.of(owner));
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            if (typeArguments == null) {
                typeArguments = Arrays
                        .stream(origin.getAnnotatedActualTypeArguments())
                        .map(TypeArgumentModel::of)
                        .collect(Collectors.toList());
            }

            return typeArguments;
        }

        @Override
        public ClassInfoModel resolve() {
            if (reference == null) {
                reference = ClassInfoModel.of(resolveRaw());
            }

            return reference;
        }

        @Override
        public void setReference(ClassInfoModel reference) {
            this.reference = reference;
        }

        private Class<?> resolveRaw() {
            return (Class<?>) ((ParameterizedType) origin.getType())
                    .getRawType();
        }
    }

    static class AnnotatedBare
            extends ClassRefSignatureReflectionModel<AnnotatedType> {
        private ClassInfoModel reference;

        public AnnotatedBare(AnnotatedType origin) {
            super(origin);
        }

        @Override
        public String getClassName() {
            return ((Class<?>) origin.getType()).getName();
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            var owner = origin.getAnnotatedOwnerType();

            if (owner == null) {
                return Optional.empty();
            }

            return Optional.of(ClassRefSignatureModel.of(owner));
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            return List.of();
        }

        @Override
        public ClassInfoModel resolve() {
            if (reference == null) {
                reference = ClassInfoModel.of((Class<?>) origin.getType());
            }

            return reference;
        }

        @Override
        public void setReference(ClassInfoModel reference) {
            this.reference = reference;
        }
    }

    static class Bare extends ClassRefSignatureReflectionModel<Class<?>> {
        private ClassInfoModel reference;

        public Bare(Class<?> origin) {
            super(origin);
        }

        @Override
        public String getClassName() {
            return origin.getName();
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            return Optional.empty();
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            return List.of();
        }

        @Override
        public ClassInfoModel resolve() {
            if (reference == null) {
                reference = ClassInfoModel.of(origin);
            }

            return reference;
        }

        @Override
        public void setReference(ClassInfoModel reference) {
            this.reference = reference;
        }
    }
}
