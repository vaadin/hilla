package com.vaadin.hilla.parser.plugins.backbone.multiendpoints;

@Endpoint
public class MultiEndpointsBarEndpoint {
    public String getBar() {
        return "bar";
    }

    public MultiEndpointsSharedModel getShared() {
        return new MultiEndpointsSharedModel();
    }
}
