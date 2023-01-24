package dev.hilla.parser.models;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodInfo;

public abstract class MethodInfoModel extends AnnotatedAbstractModel implements
        Model, NamedModel, OwnedModel<ClassInfoModel>, ParameterizedModel {
    static final Comparator<MethodInfoModel> METHOD_ORDER = Comparator
            .comparing(MethodInfoModel::getName);
    private ClassInfoModel owner;
    private List<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;
    private List<TypeParameterModel> typeParameters;

    @Deprecated
    public static MethodInfoModel of(@Nonnull MethodInfo origin) {
        return new MethodInfoSourceModel(Objects.requireNonNull(origin));
    }

    public static MethodInfoModel of(@Nonnull Executable origin) {
        return Objects.requireNonNull(origin) instanceof Constructor<?>
                ? new MethodInfoReflectionModel.Constructor(
                        (Constructor<?>) origin)
                : new MethodInfoReflectionModel.Regular((Method) origin);
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

        return equalsIgnoreParameters(other)
                && getParameters().equals(other.getParameters());
    }

    public boolean equalsIgnoreParameters(MethodInfoModel other) {
        return getName().equals(other.getName())
                && getModifiers() == other.getModifiers()
                && getResultType().equals(other.getResultType())
                && getClassName().equals(other.getClassName())
                && getTypeParameters().equals(other.getTypeParameters());
    }

    public boolean equalsIgnoreParameters(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MethodInfoModel)) {
            return false;
        }

        return equalsIgnoreParameters((MethodInfoModel) obj);
    }

    public abstract String getClassName();

    @Override
    public Class<MethodInfoModel> getCommonModelClass() {
        return MethodInfoModel.class;
    }

    public abstract int getModifiers();

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = prepareOwner();
        }

        return owner;
    }

    public List<MethodParameterInfoModel> getParameters() {
        if (parameters == null) {
            parameters = prepareParameters();
        }

        return parameters;
    }

    public Stream<MethodParameterInfoModel> getParametersStream() {
        return getParameters().stream();
    }

    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = prepareResultType();
        }

        return resultType;
    }

    @Override
    public List<TypeParameterModel> getTypeParameters() {
        if (typeParameters == null) {
            typeParameters = prepareTypeParameters();
        }

        return typeParameters;
    }

    @Override
    public int hashCode() {
        return hashCodeIgnoreParameters() + 53 * getParameters().hashCode();
    }

    public int hashCodeIgnoreParameters() {
        return getName().hashCode() + 11 * getResultType().hashCode()
                + 17 * getModifiers() + 23 * getClassName().hashCode();
    }

    public abstract boolean isAbstract();

    public abstract boolean isBridge();

    public abstract boolean isConstructor();

    public abstract boolean isFinal();

    public abstract boolean isNative();

    public abstract boolean isPrivate();

    public abstract boolean isProtected();

    public abstract boolean isPublic();

    public abstract boolean isStatic();

    public abstract boolean isStrict();

    public abstract boolean isSynchronized();

    public abstract boolean isSynthetic();

    public abstract boolean isVarArgs();

    protected abstract ClassInfoModel prepareOwner();

    protected abstract List<MethodParameterInfoModel> prepareParameters();

    protected abstract SignatureModel prepareResultType();

    protected abstract List<TypeParameterModel> prepareTypeParameters();
}
