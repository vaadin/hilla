module com.vaadin.hilla.parser.core {
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.yaml;
    requires com.vaadin.hilla.parser.utils;
    requires io.github.classgraph;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires org.jspecify;
    requires jakarta.annotation;
    requires org.slf4j;
    // Jackson 2 for OpenAPI model compatibility
    requires com.fasterxml.jackson.databind;

    opens com.vaadin.hilla.parser.core to tools.jackson.databind, com.fasterxml.jackson.databind;
    exports com.vaadin.hilla.parser.core;
    exports com.vaadin.hilla.parser.models;
    exports com.vaadin.hilla.parser.models.jackson;
}
