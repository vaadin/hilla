package dev.hilla.parser.models;

public interface ReflectionModel extends Origin {
    @Override
    default boolean isReflection() {
        return true;
    }
}
