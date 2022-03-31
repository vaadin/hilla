package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class ClassRefSignatureReflectionModel
        extends AbstractAnnotatedReflectionModel<Class<?>>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    private ClassInfoModel reference;
    private List<TypeArgumentModel> typeArguments;
    private AnnotatedParameterizedType wrapper;

    public ClassRefSignatureReflectionModel(Class<?> origin) {
        this(origin, null);
    }

    public ClassRefSignatureReflectionModel(Class<?> origin, Model parent) {
        super(origin, parent);
    }

    public ClassRefSignatureReflectionModel(AnnotatedParameterizedType wrapper,
            Model parent) {
        super((Class<?>) ((ParameterizedType) wrapper.getType()).getRawType(),
                parent);
        this.wrapper = wrapper;
    }

    @Override
    public List<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = wrapper != null
                    ? Arrays.stream(wrapper.getAnnotatedActualTypeArguments())
                            .map(arg -> TypeArgumentModel.of(arg, this))
                            .collect(Collectors.toList())
                    : List.of();
        }

        return typeArguments;
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
