package com.vaadin.fusion.parser.core;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.TypeSignature;

public class TypeSignatureChecker {
    public static boolean isVoid(TypeSignature signature) {
        return signature instanceof BaseTypeSignature && ((BaseTypeSignature) signature).getType() == Void.TYPE;
    }
}
