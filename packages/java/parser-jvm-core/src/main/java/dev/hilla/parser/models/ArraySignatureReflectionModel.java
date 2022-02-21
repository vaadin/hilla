package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.util.List;

final class ArraySignatureReflectionModel
        extends AbstractModel<AnnotatedArrayType>
        implements ArraySignatureModel, ReflectionSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private SignatureModel nestedType;

    public ArraySignatureReflectionModel(AnnotatedArrayType origin,
            Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils.processTypeAnnotations(origin, this);
        }

        return annotations;
    }

    @Override
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = SignatureModel
                    .of(origin.getAnnotatedGenericComponentType(), this);
        }

        return nestedType;
    }
}
