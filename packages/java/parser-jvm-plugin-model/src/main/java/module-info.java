module com.vaadin.hilla.parser.plugins.model {
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.hilla.parser.core;
    requires com.vaadin.hilla.parser.plugins.backbone;
    requires com.vaadin.hilla.parser.utils;
    requires jakarta.annotation;
    requires org.jspecify;
    requires io.github.classgraph;
    requires io.swagger.v3.oas.models;

    opens com.vaadin.hilla.parser.plugins.model to com.vaadin.hilla.parser.core;
    exports com.vaadin.hilla.parser.plugins.model;
}
