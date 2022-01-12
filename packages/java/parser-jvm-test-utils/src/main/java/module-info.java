module com.vaadin.fusion.parser.testutils {
    requires com.fasterxml.jackson.databind;
    requires com.vaadin.fusion.parser.utils;
    requires jsr305;
    requires io.swagger.v3.oas.models;
    requires io.swagger.v3.core;
    requires org.junit.jupiter.api;

    exports com.vaadin.fusion.parser.testutils;
}
