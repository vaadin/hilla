package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class ClassRefSignatureReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedParameterizedType>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    private ClassInfoModel reference;
    private List<TypeArgumentModel> typeArguments;

    public ClassRefSignatureReflectionModel(AnnotatedParameterizedType origin,
            Model parent) {
        super(origin, parent);
    }

    @Override
    public List<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = Arrays
                    .stream(origin.getAnnotatedActualTypeArguments())
                    .map(arg -> TypeArgumentModel.of(arg, this))
                    .collect(Collectors.toList());
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

    static class Bare extends AbstractAnnotatedReflectionModel<Class<?>>
            implements ClassRefSignatureModel, ReflectionSignatureModel {
        private ClassInfoModel reference;

        public Bare(Class<?> origin, Model parent) {
            super(origin, parent);
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
