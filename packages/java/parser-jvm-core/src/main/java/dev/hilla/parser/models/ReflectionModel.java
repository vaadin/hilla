package dev.hilla.parser.models;

public interface ReflectionModel extends TypeModel {
    @Override
    default boolean isReflectionType() {
        return true;
    }
}
