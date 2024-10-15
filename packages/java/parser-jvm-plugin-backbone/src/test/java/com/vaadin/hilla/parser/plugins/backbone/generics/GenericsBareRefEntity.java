package com.vaadin.hilla.parser.plugins.backbone.generics;

public class GenericsBareRefEntity<T> {
    private T bareGenericTypeProperty;
    private GenericsBareRefEntity<T> bareRefEntityProperty;

    public T getBareGenericTypeProperty() {
        return bareGenericTypeProperty;
    }

    public GenericsBareRefEntity<T> getBareRefEntityProperty() {
        return bareRefEntityProperty;
    }
}
