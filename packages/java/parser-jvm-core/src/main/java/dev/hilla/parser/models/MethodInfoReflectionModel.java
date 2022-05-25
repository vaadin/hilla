package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class MethodInfoReflectionModel
        extends AbstractAnnotatedReflectionModel<Method>
        implements MethodInfoModel, ReflectionModel {
    private ClassInfoModel owner;
    private List<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;

    public MethodInfoReflectionModel(Method method) {
        super(method);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MethodInfoModel)) {
            return false;
        }

        var other = (MethodInfoModel) obj;

        return origin.getName().equals(other.getName())
                && getResultType().equals(other.getResultType())
                && getParameters().equals(other.getParameters())
                && origin.getDeclaringClass().getName()
                        .equals(other.getOwnerName());
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = ClassInfoModel.of(origin.getDeclaringClass());
        }

        return owner;
    }

    @Override
    public String getOwnerName() {
        return origin.getDeclaringClass().getName();
    }

    @Override
    public List<MethodParameterInfoModel> getParameters() {
        if (parameters == null) {
            parameters = Arrays.stream(origin.getParameters())
                    .map(MethodParameterInfoModel::of)
                    .collect(Collectors.toList());
        }

        return parameters;
    }

    @Override
    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = SignatureModel.of(origin.getAnnotatedReturnType());
        }

        return resultType;
    }

    @Override
    public int hashCode() {
        return origin.getName().hashCode() + 11 * getResultType().hashCode()
                + 23 * getParameters().hashCode()
                + 53 * origin.getDeclaringClass().getName().hashCode();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(origin.getModifiers());
    }

    @Override
    public boolean isBridge() {
        return origin.isBridge();
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(origin.getModifiers());
    }

    @Override
    public boolean isNative() {
        return Modifier.isNative(origin.getModifiers());
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(origin.getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(origin.getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(origin.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(origin.getModifiers());
    }

    @Override
    public boolean isStrict() {
        return Modifier.isStrict(origin.getModifiers());
    }

    @Override
    public boolean isSynchronized() {
        return Modifier.isSynchronized(origin.getModifiers());
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isVarArgs() {
        return origin.isVarArgs();
    }
}
