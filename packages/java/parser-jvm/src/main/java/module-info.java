module com.vaadin.fusion.parser.core {
    requires io.github.classgraph;
    requires jsr305;
    requires io.swagger.v3.oas.models;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires io.swagger.v3.core;
    requires slf4j.api;

    exports com.vaadin.fusion.parser.core;
    opens com.vaadin.fusion.parser.core to com.fasterxml.jackson.databind;
}
