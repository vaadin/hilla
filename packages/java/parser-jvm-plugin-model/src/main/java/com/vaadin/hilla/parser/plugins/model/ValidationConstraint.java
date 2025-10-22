package com.vaadin.hilla.parser.plugins.model;

import java.util.Map;

import org.jspecify.annotations.NonNull;

public final class ValidationConstraint {
    private final Map<String, Object> attributes;
    private final String simpleName;

    public ValidationConstraint(@NonNull String simpleName,
            Map<String, Object> attributes) {
        this.simpleName = simpleName;
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @NonNull
    public String getSimpleName() {
        return simpleName;
    }
}
