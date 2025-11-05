package com.vaadin.hilla.parser.plugins.backbone.generics;

public class GenericsExtendedRefEntity<T extends GenericsBareRefEntity<String>> {
    private T extendedGenericTypeReference;

    public T getExtendedGenericTypeReference() {
        return extendedGenericTypeReference;
    }
}
