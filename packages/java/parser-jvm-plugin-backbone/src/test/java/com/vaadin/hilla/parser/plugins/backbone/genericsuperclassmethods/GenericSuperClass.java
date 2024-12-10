package com.vaadin.hilla.parser.plugins.backbone.genericsuperclassmethods;

import com.vaadin.hilla.EndpointExposed;

@EndpointExposed
public class GenericSuperClass<T> {

    public T genericMethod(T param) {
        return null;
    }

}
