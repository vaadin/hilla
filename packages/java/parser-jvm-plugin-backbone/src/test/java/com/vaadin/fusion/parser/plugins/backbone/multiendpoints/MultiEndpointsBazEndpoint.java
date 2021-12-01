package com.vaadin.fusion.parser.plugins.backbone.multiendpoints;

@Endpoint
public class MultiEndpointsBazEndpoint {
    public String getBaz() {
        return "baz";
    }

    public MultiEndpointsSharedModel getShared() {
        return new MultiEndpointsSharedModel();
    }
}
