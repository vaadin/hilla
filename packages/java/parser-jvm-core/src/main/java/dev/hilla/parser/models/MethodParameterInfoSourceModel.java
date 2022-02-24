package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.MethodParameterInfo;

final class MethodParameterInfoSourceModel
        extends AbstractAnnotatedSourceModel<MethodParameterInfo>
        implements MethodParameterInfoModel, SourceModel {
    private SignatureModel type;

    public MethodParameterInfoSourceModel(MethodParameterInfo parameter,
            Model parent) {
        super(parameter, parent);
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

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getAnnotationInfo().stream();
    }
}
