package dev.hilla.parser.plugins.backbone.config;

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
