package com.vaadin.hilla.parser.plugins.backbone.exposed;

@EndpointExposed
public class ExposedSuperclass {
    public ExposedSuperclassEntity methodFromExposedSuperclass() {
        return new ExposedSuperclassEntity();
    }
}
