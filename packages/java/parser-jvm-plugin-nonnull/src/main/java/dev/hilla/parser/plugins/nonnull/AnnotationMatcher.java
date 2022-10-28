package dev.hilla.parser.plugins.nonnull;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Describes annotations that make class members and their signatures nullable
 * and non-nullable: their name, meaning, and score
 */
public final class AnnotationMatcher {
    private final boolean makesNullable;
    private final String name;
    private final int score;

    public AnnotationMatcher(@Nonnull String name, boolean nullable,
            int score) {
        this.name = Objects.requireNonNull(name);
        this.makesNullable = nullable;
        this.score = score;
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
     * Returns the annotation name (e.g. jakarta.annotation.Nonnull)
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
