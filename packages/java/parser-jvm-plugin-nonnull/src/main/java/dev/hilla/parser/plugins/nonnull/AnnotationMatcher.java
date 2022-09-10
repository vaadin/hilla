package dev.hilla.parser.plugins.nonnull;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Describes annotations that make class members and their signatures nullable
 * and non-nullable: their name, meaning, and score
 */
public final class AnnotationMatcher {
    /**
     * A default annotation, which corresponds to not having any annotation
     */
    public static final AnnotationMatcher DEFAULT = new AnnotationMatcher();
    private final boolean makesNullable;
    private final String name;
    private final int score;

    /**
     * A default annotation, which corresponds to not having any annotation
     */
    public AnnotationMatcher() {
        this.name = "(default)";
        this.makesNullable = true;
        this.score = 0;
    }

    public AnnotationMatcher(@Nonnull String name, boolean makesNullable,
            int score) {
        this.name = Objects.requireNonNull(name);
        this.makesNullable = makesNullable;
        this.score = score;
    }

    /**
     * Returns true if the annotation makes a member/signature non-nullable,
     * false if nullable
     */
    public boolean doesMakeNonNull() {
        return !makesNullable;
    }

    /**
     * Returns true if the annotation makes a member/signature nullable, false
     * if non-nullable
     */
    public boolean doesMakeNullable() {
        return makesNullable;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return name.equals(((AnnotationMatcher) obj).name);
    }

    /**
     * Returns the annotation name (e.g. javax.annotation.Nonnull)
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a score that allows to compare the priority between annotations
     */
    public int getScore() {
        return score;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
