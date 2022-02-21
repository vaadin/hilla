package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class ClassRefSignatureReflectionModel extends AbstractModel<Class<?>>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<TypeArgumentModel> typeArguments;
    private AnnotatedParameterizedType wrapper;
    private ClassInfoModel resolved;

    public ClassRefSignatureReflectionModel(Class<?> origin) {
        this(origin, null);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils.processTypeAnnotations(origin, this);
        }

        return annotations;
    }

    public ClassRefSignatureReflectionModel(Class<?> origin, Model parent) {
        super(origin, parent);
    }

    public ClassRefSignatureReflectionModel(AnnotatedParameterizedType wrapper,
            Model parent) {
        super((Class<?>) wrapper.getType(), parent);
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
        if (resolved == null) {
            resolved = ClassInfoModel.of(origin);
        }

        return resolved;
    }
}
