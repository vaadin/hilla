package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.MethodInfo;

public interface MethodInfoModel
        extends Model, NamedModel, AnnotatedModel, OwnedModel<ClassInfoModel> {
    static MethodInfoModel of(@Nonnull MethodInfo method) {
        return new MethodInfoSourceModel(Objects.requireNonNull(method));
    }

    static MethodInfoModel of(@Nonnull Method method) {
        return new MethodInfoReflectionModel(method);
    }

    boolean equalsIgnoreParameters(Object obj);

    boolean equalsIgnoreParameters(MethodInfoModel obj);

    String getClassName();

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return Streams.combine(getResultDependenciesStream(),
                getParameterDependenciesStream());
    }

    int getModifiers();

    default List<ClassInfoModel> getParameterDependencies() {
        return getParameterDependenciesStream().collect(Collectors.toList());
    }

    default Stream<ClassInfoModel> getParameterDependenciesStream() {
        return getParametersStream()
                .flatMap(MethodParameterInfoModel::getDependenciesStream)
                .distinct();
    }

    List<MethodParameterInfoModel> getParameters();

    default Stream<MethodParameterInfoModel> getParametersStream() {
        return getParameters().stream();
    }

    default List<ClassInfoModel> getResultDependencies() {
        return getResultDependenciesStream().collect(Collectors.toList());
    }

    default Stream<ClassInfoModel> getResultDependenciesStream() {
        return getResultType().getDependenciesStream();
    }

    SignatureModel getResultType();

    int hashCodeIgnoreParameters();

    boolean isAbstract();

    boolean isBridge();

    boolean isFinal();

    boolean isNative();

    boolean isPrivate();

    boolean isProtected();

    boolean isPublic();

    boolean isStatic();

    boolean isStrict();

    boolean isSynchronized();

    boolean isSynthetic();

    boolean isVarArgs();
}
