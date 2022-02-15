package dev.hilla.parser.models;

public interface Origin {
    default boolean isReflection() {
        return false;
    }

    default boolean isSource() {
        return false;
    }
}
