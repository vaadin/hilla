package dev.hilla.parser.plugins.model;

import java.util.Map;

import javax.annotation.Nonnull;

public final class ValidationConstraint {
    private final Map<String, Object> attributes;
    private final String simpleName;

    public ValidationConstraint(@Nonnull String simpleName,
            Map<String, Object> attributes) {
        this.simpleName = simpleName;
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public String getSimpleName() {
        return simpleName;
    }
}
