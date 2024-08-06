package com.vaadin.hilla.parser.plugins.backbone.exposed;

import com.vaadin.hilla.EndpointExposed;

@EndpointExposed
public class ExposedSuperclass {
    public ExposedSuperclassEntity methodFromExposedSuperclass() {
        return new ExposedSuperclassEntity();
    }
}
