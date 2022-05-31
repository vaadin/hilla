package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class ClassRefSignatureReflectionModel<T extends AnnotatedElement>
        extends AbstractAnnotatedReflectionModel<T>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    private ClassInfoModel reference;

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
                && getOwner().equals(other.getOwner())
                && getTypeArguments().equals(other.getTypeArguments())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public String getClassName() {
        return getOriginClassInfo().getName();
    }

    @Override
    public int hashCode() {
        return getClassName().hashCode() + 7 * getTypeArguments().hashCode()
                + 23 * getAnnotations().hashCode() + 53 * getOwner().hashCode();
    }

    @Override
    public ClassInfoModel resolve() {
        if (reference == null) {
            reference = ClassInfoModel.of(getOriginClassInfo());
        }

        return reference;
    }

    @Override
    public void setReference(ClassInfoModel reference) {
        this.reference = reference;
    }

    protected abstract Class<?> getOriginClassInfo();

    static final class Regular extends
            ClassRefSignatureReflectionModel<AnnotatedParameterizedType> {
        private List<TypeArgumentModel> typeArguments;

        public Regular(AnnotatedParameterizedType origin) {
            super(origin);
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            return Optional.ofNullable(origin.getAnnotatedOwnerType())
                    .map(ClassRefSignatureModel::of);
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
        protected Class<?> getOriginClassInfo() {
            return (Class<?>) ((ParameterizedType) origin.getType())
                    .getRawType();
        }
    }

    static class AnnotatedBare
            extends ClassRefSignatureReflectionModel<AnnotatedType> {
        public AnnotatedBare(AnnotatedType origin) {
            super(origin);
        }

        @Override
        public Optional<ClassRefSignatureModel> getOwner() {
            return Optional.ofNullable(origin.getAnnotatedOwnerType())
                    .map(ClassRefSignatureModel::of);
        }

        @Override
        public List<TypeArgumentModel> getTypeArguments() {
            return List.of();
        }

        @Override
        protected Class<?> getOriginClassInfo() {
            return (Class<?>) origin.getType();
        }
    }

    static class Bare extends ClassRefSignatureReflectionModel<Class<?>> {
        public Bare(Class<?> origin) {
            super(origin);
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
        protected Class<?> getOriginClassInfo() {
            return origin;
        }
    }
}
