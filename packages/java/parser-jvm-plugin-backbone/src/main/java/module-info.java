module com.vaadin.hilla.parser.plugins.backbone {
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;
    requires org.jspecify;
    requires tools.jackson.databind;
    requires com.fasterxml.jackson.annotation;  // Jackson 3 uses Jackson 2 annotations
    requires com.vaadin.hilla.parser.utils;
    requires transitive com.vaadin.hilla.parser.core;
    requires jakarta.annotation;
    requires org.slf4j;
    // Jackson 3 has JavaTimeModule, Jdk8Module, and ParameterNamesModule built into jackson-databind

    exports com.vaadin.hilla.parser.plugins.backbone;
    exports com.vaadin.hilla.parser.plugins.backbone.nodes;
    opens com.vaadin.hilla.parser.plugins.backbone to com.vaadin.hilla.parser.core;
    opens com.vaadin.hilla.parser.plugins.backbone.nodes to tools.jackson.databind;
}
