package com.vaadin.fusion.parser.plugins.backbone;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ValidationConstraint.Builder.class)
public final class ValidationConstraint {
    Map<String, Object> attributes;
    private String simpleName;

    private ValidationConstraint(@Nonnull String simpleName,
            @Nullable Map<String, Object> attributes) {
        this.simpleName = simpleName;
        this.attributes = attributes;
    }

    @Nullable
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public String getSimpleName() {
        return simpleName;
    }

    @JsonPOJOBuilder
    public static class Builder {
        Map<String, Object> attributes;
        String simpleName;

        ValidationConstraint build() {
            Objects.requireNonNull(simpleName);
            return new ValidationConstraint(simpleName, attributes);
        }

        Builder withAttributes(Map<String, Object> attributes) {
            if (attributes == null || attributes.isEmpty()) {
                this.attributes = null;
            } else {
                this.attributes = new HashMap<>(attributes);
            }
            return this;
        }

        Builder withSimpleName(String simpleName) {
            this.simpleName = simpleName;
            return this;
        }
    }
}
