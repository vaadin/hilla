module com.vaadin.hilla.parser.plugins.backbone {
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;
    requires org.jspecify;
    requires tools.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.vaadin.hilla.parser.utils;
    requires transitive com.vaadin.hilla.parser.core;
    requires jakarta.annotation;
    requires org.slf4j;

    exports com.vaadin.hilla.parser.plugins.backbone;
    exports com.vaadin.hilla.parser.plugins.backbone.nodes;
    opens com.vaadin.hilla.parser.plugins.backbone to com.vaadin.hilla.parser.core;
    opens com.vaadin.hilla.parser.plugins.backbone.nodes to tools.jackson.databind;
}
