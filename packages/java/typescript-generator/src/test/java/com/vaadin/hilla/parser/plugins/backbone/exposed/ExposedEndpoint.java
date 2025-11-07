package com.vaadin.hilla.parser.plugins.backbone.exposed;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class ExposedEndpoint extends OtherSuperclass
        implements OtherInterface, ExposedInterface {
    public void methodFromEndpoint() {
    }
}
