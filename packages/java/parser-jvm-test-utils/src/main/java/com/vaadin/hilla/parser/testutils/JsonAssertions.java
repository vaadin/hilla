package com.vaadin.hilla.parser.testutils;

import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JacksonException;

import com.vaadin.hilla.parser.utils.JsonPrinter;

public final class JsonAssertions {
    private final static JsonPrinter printer = new JsonPrinter();

    public static void assertEquals(Object expected, Object actual)
            throws JacksonException {
        Assertions.assertEquals(printer.pretty().writeAsString(expected),
                printer.pretty().writeAsString(actual));
    }
}
