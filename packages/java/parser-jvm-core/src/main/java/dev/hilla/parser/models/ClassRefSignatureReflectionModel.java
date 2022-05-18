package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class ClassRefSignatureReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedParameterizedType>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    private ClassInfoModel reference;
    private List<TypeArgumentModel> typeArguments;

    public ClassRefSignatureReflectionModel(AnnotatedParameterizedType origin) {
        super(origin);
    }

    @Override
    public List<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = Arrays
                    .stream(origin.getAnnotatedActualTypeArguments())
                    .map(TypeArgumentModel::of).collect(Collectors.toList());
        }

        return typeArguments;
    }

    @Override
    public ClassInfoModel resolve() {
        if (reference == null) {
            reference = ClassInfoModel
                    .of((Class<?>) ((ParameterizedType) origin.getType())
                            .getRawType());
        }

        return reference;
    }

    @Override
    public void setReference(ClassInfoModel reference) {
        this.reference = reference;
    }

    static class AnnotatedBare
            extends AbstractAnnotatedReflectionModel<AnnotatedType>
            implements ClassRefSignatureModel, ReflectionSignatureModel {
        private ClassInfoModel reference;

        public AnnotatedBare(AnnotatedType origin) {
            super(origin);
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

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + Objects.hashCode(this.reference);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AnnotatedBare other = (AnnotatedBare) obj;
            return Objects.equals(this.reference, other.reference);
        }
    }

    static class Bare extends AbstractAnnotatedReflectionModel<Class<?>>
            implements ClassRefSignatureModel, ReflectionSignatureModel {
        private ClassInfoModel reference;

        public Bare(Class<?> origin) {
            super(origin);
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
