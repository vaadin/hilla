module com.vaadin.hilla.parser.plugins.backbone {
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;
    requires jsr305;
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.hilla.parser.utils;
    requires transitive com.vaadin.hilla.parser.core;
    requires jakarta.annotation;
    requires org.slf4j;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.module.paramnames;

    exports com.vaadin.hilla.parser.plugins.backbone;
    exports com.vaadin.hilla.parser.plugins.backbone.nodes;
    opens com.vaadin.hilla.parser.plugins.backbone to com.vaadin.hilla.parser.core;
    opens com.vaadin.hilla.parser.plugins.backbone.nodes to com.fasterxml.jackson.databind;
}
