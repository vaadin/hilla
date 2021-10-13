package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassInfo;

public final class ArrayRelativeTypeSignature implements RelativeTypeSignature {
    private final ArrayTypeSignature signature;

    public ArrayRelativeTypeSignature(ArrayTypeSignature signature) {
        this.signature = signature;
    }

    public static Stream<ClassInfo> resolve(ArrayTypeSignature signature) {
        return RelativeTypeSignature
                .resolve(signature.getElementTypeSignature());
    }

    @Override
    public ArrayTypeSignature get() {
        return signature;
    }

    @Override
    public boolean isArray() {
        return true;
    }
}
