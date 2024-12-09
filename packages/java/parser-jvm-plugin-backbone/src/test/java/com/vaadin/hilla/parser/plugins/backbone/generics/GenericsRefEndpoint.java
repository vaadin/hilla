package com.vaadin.hilla.parser.plugins.backbone.generics;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class GenericsRefEndpoint<T extends GenericsBareRefEntity<String>, U extends GenericsExtendedRefEntity<GenericsBareRefEntity<String>>> {
    public T getBareReference(T ref) {
        return ref;
    }

    public U getExtendedReference(U ref) {
        return ref;
    }
}
