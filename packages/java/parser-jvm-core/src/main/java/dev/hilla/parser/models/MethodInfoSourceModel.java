package dev.hilla.parser.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.MethodInfo;

final class MethodInfoSourceModel
        extends AbstractAnnotatedSourceModel<MethodInfo>
        implements MethodInfoModel, SourceModel {
    private List<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;

    public MethodInfoSourceModel(MethodInfo method, Model parent) {
        super(method, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public List<MethodParameterInfoModel> getParameters() {
        if (parameters == null) {
            parameters = Arrays.stream(origin.getParameterInfo()).map(
                    parameter -> MethodParameterInfoModel.of(parameter, this))
                    .collect(Collectors.toList());
        }

        return parameters;
    }

    @Override
    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = SignatureModel.of(
                    origin.getTypeSignatureOrTypeDescriptor().getResultType(),
                    this);
        }

        return resultType;
    }

    @Override
    public boolean isAbstract() {
        return origin.isAbstract();
    }

    @Override
    public boolean isBridge() {
        return origin.isBridge();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
    }

    @Override
    public boolean isNative() {
        return origin.isNative();
    }

    @Override
    public boolean isPrivate() {
        return origin.isPrivate();
    }

    @Override
    public boolean isProtected() {
        return origin.isProtected();
    }

    @Override
    public boolean isPublic() {
        return origin.isPublic();
    }

    @Override
    public boolean isStatic() {
        return origin.isStatic();
    }

    @Override
    public boolean isStrict() {
        return origin.isStrict();
    }

    @Override
    public boolean isSynchronized() {
        return origin.isSynchronized();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isVarArgs() {
        return origin.isVarArgs();
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getAnnotationInfo().stream();
    }
}
