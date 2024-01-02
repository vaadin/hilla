module com.vaadin.hilla.parser.utils {
    requires jsr305;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.module.paramnames;

    exports com.vaadin.hilla.parser.utils;
    exports com.vaadin.hilla.parser.jackson;
}
