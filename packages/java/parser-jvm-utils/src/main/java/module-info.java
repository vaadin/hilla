module com.vaadin.hilla.parser.utils {
    requires org.jspecify;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    // Jackson 3 has JavaTimeModule, Jdk8Module, and ParameterNamesModule built into jackson-databind
    requires gentyref;
    // Swagger models require Jackson 2 for deserialization
    requires com.fasterxml.jackson.databind;

    exports com.vaadin.hilla.parser.utils;
    exports com.vaadin.hilla.parser.jackson;
}
