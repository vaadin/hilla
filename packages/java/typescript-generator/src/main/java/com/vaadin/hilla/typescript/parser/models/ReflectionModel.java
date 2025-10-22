package com.vaadin.hilla.typescript.parser.models;

public interface ReflectionModel extends Model {
    @Override
    default boolean isReflection() {
        return true;
    }
}
