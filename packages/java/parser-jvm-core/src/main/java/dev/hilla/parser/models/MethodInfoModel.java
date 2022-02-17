package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.MethodInfo;

public interface MethodInfoModel extends Model, Named {
    static MethodInfoModel of(@Nonnull MethodInfo method,
            @Nonnull Model parent) {
        return new MethodInfoSourceModel(Objects.requireNonNull(method),
                Objects.requireNonNull(parent));
    }

    static MethodInfoModel of(@Nonnull Method method, @Nonnull Model parent) {
        return new MethodInfoReflectionModel(method, parent);
    }

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return StreamUtils.combine(getResultDependenciesStream(),
                getParameterDependenciesStream());
    }

    default Collection<ClassInfoModel> getParameterDependencies() {
        return getParameterDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getParameterDependenciesStream() {
        return getParametersStream()
                .flatMap(MethodParameterInfoModel::getDependenciesStream)
                .distinct();
    }

    Collection<MethodParameterInfoModel> getParameters();

    default Stream<MethodParameterInfoModel> getParametersStream() {
        return getParameters().stream();
    }

    default Collection<ClassInfoModel> getResultDependencies() {
        return getResultDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getResultDependenciesStream() {
        return getResultType().getDependenciesStream();
    }

    SignatureModel getResultType();
}
