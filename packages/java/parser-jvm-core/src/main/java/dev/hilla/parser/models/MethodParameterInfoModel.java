package dev.hilla.parser.models;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodParameterInfo;

public interface MethodParameterInfoModel
        extends Model, NamedModel, AnnotatedModel, OwnedModel<MethodInfoModel> {
    static MethodParameterInfoModel of(@Nonnull MethodParameterInfo parameter) {
        return new MethodParameterInfoSourceModel(parameter);
    }

    static MethodParameterInfoModel of(@Nonnull Parameter parameter) {
        return new MethodParameterInfoReflectionModel(parameter);
    }

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return getType().getDependenciesStream();
    }

    int getModifiers();

    SignatureModel getType();

    boolean isFinal();

    boolean isMandated();

    boolean isSynthetic();
}
