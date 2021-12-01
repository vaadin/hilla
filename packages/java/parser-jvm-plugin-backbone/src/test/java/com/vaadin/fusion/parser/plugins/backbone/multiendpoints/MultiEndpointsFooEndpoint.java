package com.vaadin.fusion.parser.plugins.backbone.multiendpoints;

@Endpoint
public class MultiEndpointsFooEndpoint {
    public String getFoo() {
        return "foo";
    }

    public MultiEndpointsSharedModel getShared() {
        return new MultiEndpointsSharedModel();
    }
}
