package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.FieldInfo;

public class RelativeFieldInfo implements Relative, RelativeMember {
    private final FieldInfo origin;
    private final RelativeClassInfo host;

    public RelativeFieldInfo(FieldInfo origin) {
        this.origin = origin;
        host = new RelativeClassInfo(origin.getClassInfo());
    }

    @Override
    public FieldInfo get() {
        return origin;
    }

    @Override
    public RelativeClassInfo getHost() {
        return host;
    }

    public RelativeTypeSignature getType() {
        return RelativeTypeSignature.of(origin.getTypeSignature());
    }

    public Stream<RelativeClassInfo> getDependencies() {
        return getType().getDependencies();
    }
}
