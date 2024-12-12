module com.vaadin.hilla.parser.plugins.transfertypes {
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.hilla.parser.core;
    requires com.vaadin.hilla.parser.plugins.backbone;
    requires com.vaadin.hilla.parser.utils;
    requires com.vaadin.hilla.runtime.transfertypes;
    requires jsr305;
    requires jakarta.annotation;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;

    exports com.vaadin.hilla.parser.plugins.transfertypes;
}
