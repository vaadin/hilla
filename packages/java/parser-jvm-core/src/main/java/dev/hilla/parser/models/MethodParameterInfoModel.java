package dev.hilla.parser.models;

import java.lang.reflect.Parameter;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodParameterInfo;

public abstract class MethodParameterInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel, OwnedModel<MethodInfoModel> {
    private MethodInfoModel owner;
    private SignatureModel type;
    private Integer index;

    public static MethodParameterInfoModel of(
            @Nonnull MethodParameterInfo origin) {
        return new MethodParameterInfoSourceModel(
                Objects.requireNonNull(origin));
    }

    public static MethodParameterInfoModel of(@Nonnull Parameter origin) {
        return new MethodParameterInfoReflectionModel(
                Objects.requireNonNull(origin));
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
    public Class<MethodParameterInfoModel> getCommonModelClass() {
        return MethodParameterInfoModel.class;
    }

    public abstract int getModifiers();

    /**
     * Returns a zero-based index of the parameter in the method parameter list.
     *
     * @return the index, from 0 to n-1
     */
    public int getIndex() {
        if (index == null) {
            index = prepareIndex();
        }

        return index;
    }

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

    public abstract boolean isImplicit();

    public abstract boolean isMandated();

    public abstract boolean isSynthetic();

    protected abstract int prepareIndex();

    protected abstract MethodInfoModel prepareOwner();

    protected abstract SignatureModel prepareType();
}
