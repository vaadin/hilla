package com.vaadin.fusion.parser.plugins.backbone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

@JsonDeserialize(builder = ValidationConstraint.Builder.class)
public final class ValidationConstraint {
    private String simpleName;

    Map<String, Object> attributes;

    private ValidationConstraint(@Nonnull String simpleName,
            @Nullable Map<String, Object> attributes) {
        this.simpleName = simpleName;
        this.attributes = attributes;
    }

    @Nonnull
    public String getSimpleName() {
        return simpleName;
    }

    @Nullable
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @JsonPOJOBuilder
    public static class Builder {
        String simpleName;
        Map<String, Object> attributes;

        Builder withSimpleName(String simpleName) {
            this.simpleName = simpleName;
            return this;
        }

        Builder withAttributes(Map<String, Object> attributes) {
            if (attributes == null || attributes.isEmpty()) {
                this.attributes = null;
            } else {
                this.attributes = new HashMap<>(attributes);
            }
            return this;
        }

        ValidationConstraint build() {
            Objects.requireNonNull(simpleName);
            return new ValidationConstraint(simpleName, attributes);
        }
    }
}
