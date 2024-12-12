package com.vaadin.hilla.parser.plugins.backbone.generics;

@Endpoint
public class GenericsBareEndpoint<T> {
    public T getSomething(T something) {
        return something;
    }
}
