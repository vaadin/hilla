package com.vaadin.fusion.parser.core;

import static com.vaadin.fusion.parser.core.Resolver.resolve;

import io.github.classgraph.FieldInfo;

public class RelativeFieldInfo implements Relative {
    private final FieldInfo fieldInfo;

    RelativeFieldInfo(final FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    @Override
    public FieldInfo get() {
        return fieldInfo;
    }

    public RelativeClassStream getDependencies() {
        return RelativeClassStream.ofRaw(resolve(fieldInfo.getTypeSignature()));
    }
}
