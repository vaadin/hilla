module dev.hilla.parser.plugins.nonnull {
    requires com.fasterxml.jackson.databind;
    requires dev.hilla.parser.plugins.backbone;
    requires dev.hilla.parser.utils;
    requires jsr305;
    requires jakarta.annotation;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;
    requires dev.hilla.parser.core;

    exports dev.hilla.parser.plugins.nonnull;
}
