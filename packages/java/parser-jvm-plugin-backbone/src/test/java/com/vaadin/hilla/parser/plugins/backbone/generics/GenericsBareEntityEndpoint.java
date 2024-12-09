package com.vaadin.hilla.parser.plugins.backbone.generics;

import com.vaadin.hilla.Endpoint;

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

    public GenericsBareRefEntity<List<Float>> getBareEntityList(
            GenericsBareRefEntity<List<Float>> ref) {
        return ref;
    }

    public record GenericsRecord<T1,T2>(
    T1 first, T2 second)
    {
    }

    public GenericsRecord<String, String> getRecord(
            GenericsRecord<String, String> record) {
        return record;
    }
}
