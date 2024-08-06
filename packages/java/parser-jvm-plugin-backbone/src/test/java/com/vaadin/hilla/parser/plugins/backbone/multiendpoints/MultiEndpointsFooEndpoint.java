package com.vaadin.hilla.parser.plugins.backbone.multiendpoints;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class MultiEndpointsFooEndpoint {
    public String getFoo() {
        return "foo";
    }

    public MultiEndpointsSharedModel getShared() {
        return new MultiEndpointsSharedModel();
    }
}
