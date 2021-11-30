package com.vaadin.fusion.maven;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class ParserClassPathConfiguration {
    private String value;
    private final boolean override = false;
    private final String delimiter = ";";

    public String getValue() {
        return value;
    }

    public boolean isOverride() {
        return override;
    }

    @Nonnull
    public String getDelimiter() {
        return Objects.requireNonNull(delimiter);
    }
}
