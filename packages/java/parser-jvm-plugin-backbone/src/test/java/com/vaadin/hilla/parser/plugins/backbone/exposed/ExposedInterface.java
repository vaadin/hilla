package com.vaadin.hilla.parser.plugins.backbone.exposed;

import com.vaadin.hilla.EndpointExposed;

@EndpointExposed
public interface ExposedInterface {
    default ExposedInterfaceEntity methodFromExposedInterface() {
        return new ExposedInterfaceEntity();
    }
}
