package dev.hilla.parser.models;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodParameterInfo;

public interface MethodParameterInfoModel
        extends Model, NamedModel, AnnotatedModel {
    static MethodParameterInfoModel of(@Nonnull MethodParameterInfo parameter,
            @Nonnull Model parent) {
        return new MethodParameterInfoSourceModel(parameter, parent);
    }

    static MethodParameterInfoModel of(@Nonnull Parameter parameter,
            @Nonnull Model parent) {
        return new MethodParameterInfoReflectionModel(parameter, parent);
    }

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return getType().getDependenciesStream();
    }

    SignatureModel getType();
}
