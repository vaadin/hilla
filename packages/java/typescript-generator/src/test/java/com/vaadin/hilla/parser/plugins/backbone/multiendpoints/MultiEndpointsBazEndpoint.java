package com.vaadin.hilla.parser.plugins.backbone.multiendpoints;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class MultiEndpointsBazEndpoint {
    public String getBaz() {
        return "baz";
    }

    public MultiEndpointsSharedModel getShared() {
        return new MultiEndpointsSharedModel();
    }
}
