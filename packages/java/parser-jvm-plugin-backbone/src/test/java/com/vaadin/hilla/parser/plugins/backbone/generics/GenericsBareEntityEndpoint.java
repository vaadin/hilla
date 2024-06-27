package com.vaadin.hilla.parser.plugins.backbone.generics;

@Endpoint
public class GenericsBareEntityEndpoint {
    public GenericsBareRefEntity<String> getBareReference(
            GenericsBareRefEntity<String> ref) {
        return ref;
    }
}
