package dev.hilla.parser.models;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodParameterInfo;

public abstract class MethodParameterInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel, OwnedModel<MethodInfoModel> {
    private MethodInfoModel owner;
    private SignatureModel type;

    public static MethodParameterInfoModel of(
            @Nonnull MethodParameterInfo parameter) {
        return new MethodParameterInfoSourceModel(parameter);
    }

    public static MethodParameterInfoModel of(@Nonnull Parameter parameter) {
        return new MethodParameterInfoReflectionModel(parameter);
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
                && getModifiers() == other.getModifiers()
                && getType().equals(other.getType())
                && getName().equals(other.getName());
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        return getType().getDependenciesStream();
    }

    public abstract int getModifiers();

    @Override
    public MethodInfoModel getOwner() {
        if (owner == null) {
            owner = prepareOwner();
        }

        return owner;
    }

    public SignatureModel getType() {
        if (type == null) {
            type = prepareType();
        }

        return type;
    }

    @Override
    public int hashCode() {
        return getOwner().hashCodeIgnoreParameters()
                + 11 * getAnnotations().hashCode() + 17 * getModifiers()
                + 23 * getType().hashCode() + 53 * getName().hashCode();
    }

    public abstract boolean isFinal();

    public abstract boolean isMandated();

    public abstract boolean isImplicit();

    public abstract boolean isSynthetic();

    protected abstract MethodInfoModel prepareOwner();

    protected abstract SignatureModel prepareType();
}
