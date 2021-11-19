module com.vaadin.fusion.parser.plugins.backbone {
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;
    requires jsr305;
    requires com.vaadin.fusion.parser.core;
    requires com.fasterxml.jackson.databind;

    opens com.vaadin.fusion.parser.plugins.backbone to com.vaadin.fusion.parser.core;
    exports com.vaadin.fusion.parser.plugins.backbone;
}
