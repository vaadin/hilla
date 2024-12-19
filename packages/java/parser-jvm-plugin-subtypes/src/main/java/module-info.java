module com.vaadin.hilla.parser.plugins.subtypes {
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.hilla.parser.plugins.backbone;
    requires com.vaadin.hilla.parser.utils;
    requires org.jspecify;
    requires jakarta.annotation;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;
    requires com.vaadin.hilla.parser.core;

    exports com.vaadin.hilla.parser.plugins.subtypes;
}
