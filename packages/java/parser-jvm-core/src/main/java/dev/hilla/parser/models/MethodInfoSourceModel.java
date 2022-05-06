package dev.hilla.parser.models;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.MethodInfo;

final class MethodInfoSourceModel
        extends AbstractAnnotatedSourceModel<MethodInfo>
        implements MethodInfoModel, SourceModel {
    private ClassInfoModel owner;
    private List<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;

    public MethodInfoSourceModel(MethodInfo method) {
        super(method);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof MethodInfoModel)) {
            return false;
        }

        if (other instanceof MethodInfoSourceModel) {
            return Objects.equals(origin,
                    ((MethodInfoSourceModel) other).origin);
        }

        return Objects.equals(getName(), ((MethodInfoModel) other).getName());
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = ClassInfoModel.of(origin.getClassInfo());
        }

        return owner;
    }

    @Override
    public List<MethodParameterInfoModel> getParameters() {
        if (parameters == null) {
            parameters = Arrays.stream(origin.getParameterInfo())
                    .map(MethodParameterInfoModel::of)
                    .collect(Collectors.toList());
        }

        return parameters;
    }

    @Override
    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = SignatureModel.of(
                    origin.getTypeSignatureOrTypeDescriptor().getResultType());
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
