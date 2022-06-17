package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

final class MethodParameterInfoReflectionModel
        extends AbstractAnnotatedReflectionModel<Parameter>
        implements MethodParameterInfoModel, ReflectionModel {
    private MethodInfoModel owner;
    private SignatureModel type;

    public MethodParameterInfoReflectionModel(Parameter parameter) {
        super(parameter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MethodParameterInfoModel)) {
            return false;
        }

        var other = (MethodParameterInfoModel) obj;

        return getOwner().equalsIgnoreParameters(other.getOwner())
                && getAnnotations().equals(other.getAnnotations())
                && origin.getModifiers() == other.getModifiers()
                && getType().equals(other.getType())
                && origin.getName().equals(other.getName());
    }

    @Override
    public int getModifiers() {
        return origin.getModifiers();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public MethodInfoModel getOwner() {
        if (owner == null) {
            owner = MethodInfoModel
                    .of((Method) origin.getDeclaringExecutable());
        }

        return owner;
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getAnnotatedType());
        }

        return type;
    }

    @Override
    public int hashCode() {
        return getOwner().hashCodeIgnoreParameters()
                + 11 * getAnnotations().hashCode() + 17 * origin.getModifiers()
                + 23 * getType().hashCode() + 53 * origin.getName().hashCode();
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(origin.getModifiers());
    }

    @Override
    public boolean isMandated() {
        return (origin.getModifiers() & '耀') != 0;
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }
}
