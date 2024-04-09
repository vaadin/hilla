package com.vaadin.hilla.parser.plugins.backbone.generics;

public interface GenericInterface<T> {
    T dealWithGenericType(T object);

    T dealWithItAgain(T object);
}
