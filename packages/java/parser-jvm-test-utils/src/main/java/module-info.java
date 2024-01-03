module com.vaadin.hilla.parser.testutils {
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.hilla.parser.utils;
    requires jsr305;
    requires io.swagger.v3.oas.models;
    requires io.swagger.v3.core;
    requires org.junit.jupiter.api;

    exports com.vaadin.hilla.parser.testutils;
}
