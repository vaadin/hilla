package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.FieldInfo;

public class RelativeFieldInfo implements Relative, RelativeMember {
    private final RelativeClassInfo host;
    private final FieldInfo origin;

    public RelativeFieldInfo(FieldInfo origin) {
        this.origin = origin;
        host = new RelativeClassInfo(origin.getClassInfo());
    }

    @Override
    public FieldInfo get() {
        return origin;
    }

    public Stream<RelativeClassInfo> getDependencies() {
        return getType().getDependencies();
    }

    @Override
    public RelativeClassInfo getHost() {
        return host;
    }

    public RelativeTypeSignature getType() {
        return RelativeTypeSignature.of(origin.getTypeSignature());
    }
}
