module com.vaadin.hilla.parser.utils {
    requires org.jspecify;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires gentyref;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires org.yaml.snakeyaml;

    exports com.vaadin.hilla.parser.utils;
    exports com.vaadin.hilla.parser.jackson;
}
