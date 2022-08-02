package dev.hilla.parser.plugins.nonnull;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Describes a (not)null-related annotations: its name, meaning, and score
 */
public class AnnotationMatcher {
    private final String name;
    private final boolean isNull;
    private final int score;

    public AnnotationMatcher(@Nonnull String name, boolean isNull, int score) {
        this.name = Objects.requireNonNull(name);
        this.isNull = isNull;
        this.score = score;
    }

    /**
     * Returns the annotation name (e.g. javax.annotation.Nonnull)
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if the annotation means nullable, false if it means notnull
     */
    public boolean isNull() {
        return isNull;
    }

    /**
     * Returns true if the annotation means notnull, false if it means nullable
     */
    public boolean isNonNull() {
        return !isNull;
    }

    /**
     * Returns a score that allows to compare the priority between annotations
     */
    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return name.equals(((AnnotationMatcher) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * A default annotation, which corresponds to not having any annotation
     */
    public static final AnnotationMatcher DEFAULT = new AnnotationMatcher(
            "(default)", true, 0);
}
