package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

final class ArraySignatureReflectionModel extends ArraySignatureModel
        implements ReflectionSignatureModel {
    private final AnnotatedArrayType origin;

    ArraySignatureReflectionModel(AnnotatedArrayType origin) {
        this.origin = origin;
    }

    @Override
    public AnnotatedElement get() {
        return origin;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected SignatureModel prepareNestedType() {
        return SignatureModel.of(origin.getAnnotatedGenericComponentType());
    }
}
