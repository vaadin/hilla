package com.vaadin.fusion.parser.core;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodInfo;

public class RelativeMethodInfo
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
    public Stream<RelativeClassInfo> getDependencies() {
        return Stream.of(getResultDependencies(), getParameterDependencies())
                .flatMap(Function.identity()).distinct();
    }

    public Stream<RelativeClassInfo> getParameterDependencies() {
        return getParameters().stream()
                .flatMap(RelativeMethodParameterInfo::getDependencies)
                .distinct();
    }

    public List<RelativeMethodParameterInfo> getParameters() {
        return parameters;
    }

    public Stream<RelativeClassInfo> getResultDependencies() {
        return getResultType().getDependencies();
    }

    public RelativeTypeSignature getResultType() {
        return resultType;
    }
}
