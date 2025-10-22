package com.vaadin.hilla.parser.plugins.backbone.generics;

import java.lang.reflect.Modifier;

@Endpoint
public class ImplementInterfaceEndpoint
        implements GenericInterface<ConcreteType> {

    @Override
    public ConcreteType dealWithGenericType(ConcreteType object) {
        return object;
    }

    @Override
    public ConcreteType dealWithItAgain(ConcreteType object) {
        return object;
    }

    public ConcreteType dealWithConcreteType(ConcreteType object) {
        return object;
    }
}
