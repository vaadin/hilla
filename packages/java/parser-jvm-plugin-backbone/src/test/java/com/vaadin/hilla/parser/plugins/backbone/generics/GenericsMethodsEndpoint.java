package com.vaadin.hilla.parser.plugins.backbone.generics;

import java.util.List;

@Endpoint
public class GenericsMethodsEndpoint {
    public <T extends List<String>> T getList(T list) {
        return list;
    }

    public <T extends GenericsBareRefEntity<String>> T getRef(T ref) {
        return ref;
    }

    public <T> T getValueWithGenericType(T something) {
        return something;
    }
}
