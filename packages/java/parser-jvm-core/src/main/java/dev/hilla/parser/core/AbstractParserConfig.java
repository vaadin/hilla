package dev.hilla.parser.core;

import java.util.Objects;
import java.util.Set;

import jakarta.annotation.Nonnull;

import io.swagger.v3.oas.models.OpenAPI;

public abstract class AbstractParserConfig {
    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }

        if (!(another instanceof AbstractParserConfig)) {
            return false;
        }

        return Objects.equals(getClassPathElements(),
                ((AbstractParserConfig) another).getClassPathElements())
                && Objects.equals(getEndpointAnnotationName(),
                        ((AbstractParserConfig) another)
                                .getEndpointAnnotationName())
                && Objects.equals(getEndpointExposedAnnotationName(),
                        ((AbstractParserConfig) another)
                                .getEndpointExposedAnnotationName())
                && Objects.equals(getOpenAPI(),
                        ((AbstractParserConfig) another).getOpenAPI())
                && Objects.equals(getPlugins(),
                        ((AbstractParserConfig) another).getPlugins());
    }

    @Nonnull
    public abstract Set<String> getClassPathElements();

    @Nonnull
    public abstract String getEndpointAnnotationName();

    @Nonnull
    public abstract String getEndpointExposedAnnotationName();

    @Nonnull
    public abstract OpenAPI getOpenAPI();

    @Nonnull
    public abstract Set<Plugin> getPlugins();

    @Override
    public int hashCode() {
        return Objects.hash(getClassPathElements(), getEndpointAnnotationName(),
                getEndpointExposedAnnotationName(), getOpenAPI(), getPlugins());
    }
}
