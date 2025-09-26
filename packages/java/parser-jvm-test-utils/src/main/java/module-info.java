open module com.vaadin.hilla.parser.testutils {
    requires tools.jackson.databind;
    requires com.vaadin.hilla.parser.utils;
    requires org.jspecify;
    requires io.swagger.v3.oas.models;
    requires io.swagger.v3.core;
    requires org.junit.jupiter.api;
    // JSONAssert dependencies will be resolved as unnamed/automatic modules at runtime

    exports com.vaadin.hilla.parser.testutils;
}
