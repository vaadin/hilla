package com.vaadin.hilla.parser.plugins.backbone.emptyentity;

@Endpoint
public class EmptyEntityEndpoint {
    public EmptyEntity getEmpty() {
        return new EmptyEntity();
    }
}
