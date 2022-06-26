package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.MethodInfo;

public abstract class MethodInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel, OwnedModel<ClassInfoModel> {
    private ClassInfoModel owner;
    private List<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;

    public static MethodInfoModel of(@Nonnull MethodInfo method) {
        return new MethodInfoSourceModel(Objects.requireNonNull(method));
    }

    public static MethodInfoModel of(@Nonnull Method method) {
        return new MethodInfoReflectionModel(method);
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

    public abstract boolean equalsIgnoreParameters(MethodInfoModel obj);

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
    public Stream<ClassInfoModel> getDependenciesStream() {
        return Streams.combine(getResultDependenciesStream(),
                getParameterDependenciesStream());
    }

    public abstract int getModifiers();

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = prepareOwner();
        }

        return owner;
    }

    public List<ClassInfoModel> getParameterDependencies() {
        return getParameterDependenciesStream().collect(Collectors.toList());
    }

    public Stream<ClassInfoModel> getParameterDependenciesStream() {
        return getParametersStream()
                .flatMap(MethodParameterInfoModel::getDependenciesStream)
                .distinct();
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

    public List<ClassInfoModel> getResultDependencies() {
        return getResultDependenciesStream().collect(Collectors.toList());
    }

    public Stream<ClassInfoModel> getResultDependenciesStream() {
        return getResultType().getDependenciesStream();
    }

    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = prepareResultType();
        }

        return resultType;
    }

    @Override
    public int hashCode() {
        return hashCodeIgnoreParameters() + 53 * getParameters().hashCode();
    }

    public abstract int hashCodeIgnoreParameters();

    public abstract boolean isAbstract();

    public abstract boolean isBridge();

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
}
