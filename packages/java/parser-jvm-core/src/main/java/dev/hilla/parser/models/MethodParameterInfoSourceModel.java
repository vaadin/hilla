package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.MethodParameterInfo;

final class MethodParameterInfoSourceModel
        extends AbstractModel<MethodParameterInfo>
        implements MethodParameterInfoModel, SourceModel {
    private List<AnnotationInfoModel> annotations;
    private SignatureModel type;

    public MethodParameterInfoSourceModel(MethodParameterInfo parameter,
            Model parent) {
        super(parameter, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils
                .processTypeAnnotations(origin.getAnnotationInfo(), this);
        }

        return annotations;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor(),
                    this);
        }

        return type;
    }
}
