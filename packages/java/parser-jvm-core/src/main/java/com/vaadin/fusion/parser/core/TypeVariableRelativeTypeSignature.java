package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeVariableSignature;

public final class TypeVariableRelativeTypeSignature
        extends AbstractRelative<TypeVariableSignature, Relative<?>>
        implements RelativeTypeSignature {
    private RelativeTypeParameter typeParameter;

    TypeVariableRelativeTypeSignature(TypeVariableSignature origin,
            Relative<?> parent) {
        super(origin, parent);
    }

    public static Stream<ClassInfo> resolve(TypeVariableSignature signature) {
        // We can resolve only the type variable class bound here (bound class
        // is `com.vaadin.fusion.X` in `T extends com.vaadin.fusion.X`)
        var bound = signature.resolve().getClassBound();

        return bound != null ? RelativeTypeSignature.resolve(bound)
                : Stream.empty();
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }

    public RelativeTypeParameter resolve() {
        if (typeParameter == null) {
            typeParameter = new RelativeTypeParameter(origin.resolve(), this);
        }

        return typeParameter;
    }
}
