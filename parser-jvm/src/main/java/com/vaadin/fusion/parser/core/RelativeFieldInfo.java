package com.vaadin.fusion.parser.core;

import static com.vaadin.fusion.parser.core.Resolver.resolve;

import io.github.classgraph.FieldInfo;

public class RelativeFieldInfo implements Relative, RelativeMember {
    private final FieldInfo fieldInfo;
    private final RelativeClassInfo host;

    RelativeFieldInfo(final FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
        host = new RelativeClassInfo(fieldInfo.getClassInfo());
    }

    @Override
    public FieldInfo get() {
        return fieldInfo;
    }

    @Override
    public RelativeClassInfo getHost() {
        return new RelativeClassInfo(fieldInfo.getClassInfo());
    }

    public RelativeClassStream getDependencies() {
        return RelativeClassStream.ofRaw(resolve(fieldInfo.getTypeSignature()));
    }
}
