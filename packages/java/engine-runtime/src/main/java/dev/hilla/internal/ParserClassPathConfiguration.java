package dev.hilla.internal;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParserClassPathConfiguration that = (ParserClassPathConfiguration) o;
        return override == that.override &&
            Objects.equals(delimiter, that.delimiter) &&
            Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delimiter, override, value);
    }
}
