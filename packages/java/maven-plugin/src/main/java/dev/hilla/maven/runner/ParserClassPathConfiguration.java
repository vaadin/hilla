package dev.hilla.maven.runner;

import java.util.Objects;

import javax.annotation.Nonnull;

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
