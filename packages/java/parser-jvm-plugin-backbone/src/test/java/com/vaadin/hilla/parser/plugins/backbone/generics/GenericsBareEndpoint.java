package com.vaadin.hilla.parser.plugins.backbone.generics;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class GenericsBareEndpoint<T> {
    public T getSomething(T something) {
        return something;
    }
}
