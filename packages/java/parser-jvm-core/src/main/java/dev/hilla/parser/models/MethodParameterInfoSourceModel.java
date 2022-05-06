package dev.hilla.parser.models;

import java.util.Objects;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.MethodParameterInfo;

final class MethodParameterInfoSourceModel
        extends AbstractAnnotatedSourceModel<MethodParameterInfo>
        implements MethodParameterInfoModel, SourceModel {
    private MethodInfoModel owner;
    private SignatureModel type;

    public MethodParameterInfoSourceModel(MethodParameterInfo parameter) {
        super(parameter);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof MethodParameterInfoModel)) {
            return false;
        }

        if (other instanceof MethodParameterInfoSourceModel) {
            return Objects.equals(origin,
                    ((MethodParameterInfoSourceModel) other).origin);
        }

        return Objects.equals(getName(),
                ((MethodParameterInfoModel) other).getName());
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public MethodInfoModel getOwner() {
        if (owner == null) {
            owner = MethodInfoModel.of(origin.getMethodInfo());
        }

        return owner;
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor());
        }

        return type;
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getAnnotationInfo().stream();
    }
}
