package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeSignature;
import io.github.classgraph.TypeVariableSignature;

public final class TypeVariableRelativeTypeSignature
        implements RelativeTypeSignature {
    public static Stream<ClassInfo> resolve(TypeVariableSignature signature) {
        // We can resolve only the type variable class bound here (bound class
        // is `com.vaadin.fusion.X` in `T extends com.vaadin.fusion.X`)
        TypeSignature bound = signature.resolve().getClassBound();

        return bound != null ? RelativeTypeSignature.resolve(bound) : Stream.empty();
    }

    private final TypeVariableSignature signature;

    public TypeVariableRelativeTypeSignature(TypeVariableSignature signature) {
        this.signature = signature;
    }

    @Override
    public TypeSignature get() {
        return signature;
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }
}
