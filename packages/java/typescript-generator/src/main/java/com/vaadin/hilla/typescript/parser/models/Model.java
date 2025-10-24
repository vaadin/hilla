package com.vaadin.hilla.typescript.parser.models;

public interface Model {
    Object get();

    Class<? extends Model> getCommonModelClass();

    default boolean isReflection() {
        return false;
    }

    default boolean isSource() {
        return false;
    }
}
