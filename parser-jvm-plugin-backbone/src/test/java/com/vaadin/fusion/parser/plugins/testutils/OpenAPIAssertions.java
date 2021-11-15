package com.vaadin.fusion.parser.plugins.testutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Assertions;

import com.vaadin.fusion.parser.core.OpenAPIPrinter;

public final class OpenAPIAssertions {
    private final static OpenAPIPrinter printer = new OpenAPIPrinter();

    public static void assertEquals(OpenAPI expected, OpenAPI actual)
            throws JsonProcessingException {
        Assertions.assertEquals(printer.pretty().writeAsString(expected),
                printer.pretty().writeAsString(actual));
    }
}
