package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.util.List;

final class ArraySignatureReflectionModel
        extends ArraySignatureAbstractModel<AnnotatedArrayType>
        implements ReflectionSignatureModel {
    public ArraySignatureReflectionModel(AnnotatedArrayType origin) {
        super(origin);
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return AnnotationUtils.convert(origin.getAnnotations());
    }

    @Override
    protected SignatureModel prepareNestedType() {
        return SignatureModel.of(origin.getAnnotatedGenericComponentType());
    }
}
