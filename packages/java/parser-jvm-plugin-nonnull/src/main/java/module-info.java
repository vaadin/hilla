module com.vaadin.fusion.parser.plugins.nonnull {
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.fusion.parser.core;
    requires com.vaadin.fusion.parser.plugins.backbone;
    requires com.vaadin.fusion.parser.utils;
    requires jsr305;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;

    exports com.vaadin.fusion.parser.plugins.nonnull;
}
