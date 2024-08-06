package com.vaadin.hilla.parser.plugins.backbone.config;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class CustomConfigEndpoint {
    public CustomConfigEntity get() {
        return new CustomConfigEntity();
    }

    public static class CustomConfigEntity {
        private int bar;
        private String foo;
    }
}
