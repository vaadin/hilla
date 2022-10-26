module dev.hilla.parser.plugins.transfertypes {
    requires com.fasterxml.jackson.databind;
    requires dev.hilla.parser.core;
    requires dev.hilla.parser.plugins.backbone;
    requires dev.hilla.parser.utils;
    requires dev.hilla.runtime.transfertypes;
    requires jsr305;
    requires jakarta.annotation;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;

    exports dev.hilla.parser.plugins.transfertypes;
}
