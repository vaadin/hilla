package dev.hilla.parser.plugins.backbone.exposed;

public interface OtherInterface {
    default OtherEntity methodFromOtherInterface() {
        return new OtherEntity();
    }
}
