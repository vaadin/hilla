package com.vaadin.hilla.parser.plugins.model;

import javax.annotation.Nonnull;
import java.util.Map;

public final class Annotation {
    private final Map<String, Object> attributes;
    private final String name;

    public Annotation(@Nonnull String name, Map<String, Object> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public String getName() {
        return name;
    }
}
