package com.vaadin.hilla.parser.plugins.backbone.generics;

import java.util.List;

@Endpoint
public class GenericsBareEntityEndpoint {
    public GenericsBareRefEntity<String> getBareReference(
            GenericsBareRefEntity<String> ref) {
        return ref;
    }

    public record GenericsBareEntity(String bareEntityProperty) {
    }

    public GenericsBareRefEntity<GenericsBareEntity> getBareEntity(
            GenericsBareRefEntity<GenericsBareEntity> ref) {
        return ref;
    }

    public GenericsBareRefEntity<List<String>> getBareEntityList(
            GenericsBareRefEntity<List<String>> ref) {
        return ref;
    }
}
