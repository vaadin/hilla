package dev.hilla.parser.plugins.backbone.exposed;

@EndpointExposed
public interface ExposedInterface {
    default ExposedEntity methodFromExposedInterface() {
        return new ExposedEntity();
    }
}
