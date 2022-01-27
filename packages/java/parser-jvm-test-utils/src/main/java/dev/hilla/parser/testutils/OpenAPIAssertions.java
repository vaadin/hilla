package dev.hilla.parser.testutils;

import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.hilla.parser.utils.OpenAPIPrinter;
import io.swagger.v3.oas.models.OpenAPI;

public final class OpenAPIAssertions {
    private final static OpenAPIPrinter printer = new OpenAPIPrinter();

    public static void assertEquals(OpenAPI expected, OpenAPI actual)
            throws JsonProcessingException {
        Assertions.assertEquals(printer.pretty().writeAsString(expected),
                printer.pretty().writeAsString(actual));
    }
}
