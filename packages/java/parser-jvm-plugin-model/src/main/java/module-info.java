module com.vaadin.fusion.parser.plugins.model {
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.fusion.parser.core;
    requires com.vaadin.fusion.parser.plugins.backbone;
    requires com.vaadin.fusion.parser.utils;
    requires jsr305;
    requires io.github.classgraph;
    requires io.swagger.v3.oas.models;

    opens com.vaadin.fusion.parser.plugins.model to com.vaadin.fusion.parser.core;
    exports com.vaadin.fusion.parser.plugins.model;
}
