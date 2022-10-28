module dev.hilla.parser.plugins.model {
    requires com.fasterxml.jackson.databind;
    requires dev.hilla.parser.core;
    requires dev.hilla.parser.plugins.backbone;
    requires dev.hilla.parser.utils;
    requires jakarta.annotation;
    requires jsr305;
    requires io.github.classgraph;
    requires io.swagger.v3.oas.models;

    opens dev.hilla.parser.plugins.model to dev.hilla.parser.core;
    exports dev.hilla.parser.plugins.model;
}
