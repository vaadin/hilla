package com.vaadin.hilla.parser.plugins.model;

import org.jspecify.annotations.NonNull;
import java.util.Map;

public final class Annotation {
    private final Map<String, Object> attributes;
    private final String name;

    public Annotation(@NonNull String name, Map<String, Object> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
