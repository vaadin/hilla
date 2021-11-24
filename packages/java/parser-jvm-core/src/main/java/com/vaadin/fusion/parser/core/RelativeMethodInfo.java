package com.vaadin.fusion.parser.core;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodInfo;

public final class RelativeMethodInfo
        extends AbstractRelative<MethodInfo, RelativeClassInfo> {
    private final RelativeTypeSignature resultType;
    private final List<RelativeMethodParameterInfo> parameters;

    public RelativeMethodInfo(@Nonnull MethodInfo origin,
            @Nonnull RelativeClassInfo parent) {
        super(origin, Objects.requireNonNull(parent));

        parameters = Arrays.stream(origin.getParameterInfo()).map(
                parameter -> new RelativeMethodParameterInfo(parameter, this))
                .collect(Collectors.toList());
        resultType = RelativeTypeSignature.of(
                origin.getTypeSignatureOrTypeDescriptor().getResultType(),
                this);
    }

    @Override
    public Stream<RelativeClassInfo> getDependenciesStream() {
        return Stream.of(getResultDependenciesStream(), getParameterDependenciesStream())
                .flatMap(Function.identity()).distinct();
    }

    public List<RelativeClassInfo> getParameterDependencies() {
        return getParameterDependenciesStream().collect(Collectors.toList());
    }

    public Stream<RelativeClassInfo> getParameterDependenciesStream() {
        return parameters.stream()
                .flatMap(RelativeMethodParameterInfo::getDependenciesStream)
                .distinct();
    }

    public List<RelativeMethodParameterInfo> getParameters() {
        return parameters;
    }

    public Stream<RelativeMethodParameterInfo> getParametersStream() {
        return parameters.stream();
    }

    public List<RelativeClassInfo> getResultDependencies() {
        return getResultDependenciesStream().collect(Collectors.toList());
    }

    public Stream<RelativeClassInfo> getResultDependenciesStream() {
        return getResultType().getDependenciesStream();
    }

    public RelativeTypeSignature getResultType() {
        return resultType;
    }
}
