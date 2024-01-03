module com.vaadin.hilla.parser.core {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.vaadin.hilla.parser.utils;
    requires io.github.classgraph;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires jsr305;
    requires jakarta.annotation;
    requires org.slf4j;

    opens com.vaadin.hilla.parser.core to com.fasterxml.jackson.databind;
    exports com.vaadin.hilla.parser.core;
    exports com.vaadin.hilla.parser.models;
    exports com.vaadin.hilla.parser.models.jackson;
}
