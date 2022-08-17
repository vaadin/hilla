package dev.hilla.parser.plugins.backbone.exposed;

@EndpointExposed
public class ExposedSuperclass {
    public ExposedEntity methodFromExposedSuperclass() {
        return new ExposedEntity();
    }
}
