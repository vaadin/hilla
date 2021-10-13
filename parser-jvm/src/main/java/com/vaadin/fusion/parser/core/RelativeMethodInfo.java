package com.vaadin.fusion.parser.core;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.classgraph.MethodInfo;

public class RelativeMethodInfo
        extends AbstractRelative<MethodInfo, RelativeClassInfo> {
    private final RelativeTypeSignature resultType;

    public RelativeMethodInfo(MethodInfo origin, RelativeClassInfo parent) {
        super(origin, parent);

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
        return getParameters()
                .flatMap(RelativeMethodParameterInfo::getDependencies)
                .distinct();
    }

    public Stream<RelativeMethodParameterInfo> getParameters() {
        return Arrays.stream(origin.getParameterInfo()).map(
                parameter -> new RelativeMethodParameterInfo(parameter, this));
    }

    public Stream<RelativeClassInfo> getResultDependencies() {
        return getResultType().getDependencies();
    }

    public RelativeTypeSignature getResultType() {
        return resultType;
    }
}
