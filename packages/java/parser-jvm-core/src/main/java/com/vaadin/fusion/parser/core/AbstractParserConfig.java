package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import io.swagger.v3.oas.models.OpenAPI;

public abstract class AbstractParserConfig {
    @Nonnull
    public abstract Set<String> getClassPathElements();

    @Nonnull
    public abstract String getEndpointAnnotationName();

    @Nonnull
    public abstract OpenAPI getOpenAPI();

    @Nonnull
    public abstract Set<String> getPlugins();

    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }

        if (!(another instanceof AbstractParserConfig)) {
            return false;
        }

        return Objects.equals(getClassPathElements(), ((AbstractParserConfig) another).getClassPathElements())
            && Objects.equals(getEndpointAnnotationName(),
            ((AbstractParserConfig) another).getEndpointAnnotationName())
            && Objects.equals(getOpenAPI(), ((AbstractParserConfig) another).getOpenAPI())
            && Objects.equals(getPlugins(), ((AbstractParserConfig) another).getPlugins());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassPathElements(), getEndpointAnnotationName(), getOpenAPI(), getPlugins());
    }
}
