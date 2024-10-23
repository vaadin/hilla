package com.vaadin.hilla.parser.plugins.backbone.multiendpoints;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class MultiEndpointsBarEndpoint {
    public String getBar() {
        return "bar";
    }

    public MultiEndpointsSharedModel getShared() {
        return new MultiEndpointsSharedModel();
    }
}
