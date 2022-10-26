package dev.hilla.maven;

import java.util.Objects;

import jakarta.annotation.Nonnull;

public final class ParserClassPathConfiguration {
    private final String delimiter = ";";
    private final boolean override = false;
    private String value;

    @Nonnull
    public String getDelimiter() {
        return Objects.requireNonNull(delimiter);
    }

    public String getValue() {
        return value;
    }

    public boolean isOverride() {
        return override;
    }
}
