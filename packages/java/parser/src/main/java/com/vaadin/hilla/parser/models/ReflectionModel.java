package com.vaadin.hilla.parser.models;

public interface ReflectionModel extends Model {
    @Override
    default boolean isReflection() {
        return true;
    }
}
