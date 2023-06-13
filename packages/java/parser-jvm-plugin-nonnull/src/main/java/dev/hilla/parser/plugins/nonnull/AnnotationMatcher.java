package dev.hilla.parser.plugins.nonnull;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Describes annotations that make class members and their signatures nullable
 * and non-nullable: their name, meaning, and score
 */
public final class AnnotationMatcher {
    private boolean makesNullable;
    private String name;
    private int score;

    public AnnotationMatcher() {
    }

    public AnnotationMatcher(@Nonnull String name, boolean makesNullable,
            int score) {
        this.name = Objects.requireNonNull(name);
        this.makesNullable = makesNullable;
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

    public void setMakesNullable(boolean makesNullable) {
        this.makesNullable = makesNullable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
