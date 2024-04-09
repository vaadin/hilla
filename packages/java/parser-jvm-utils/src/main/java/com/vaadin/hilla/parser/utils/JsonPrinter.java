package com.vaadin.hilla.parser.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.v3.core.util.Json;

public final class JsonPrinter {
    private final ObjectMapper mapper = Json.mapper();
    private final Pretty pretty;

    public JsonPrinter() {
        // Putting the `pretty` initialization here allows preserving correct
        // class initialization sequence.
        pretty = new Pretty();
    }

    public Pretty pretty() {
        return pretty;
    }

    public String writeAsString(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }

    public final class Pretty {
        private final ObjectWriter writer;

        private Pretty() {
            var printer = new DefaultPrettyPrinter();
            var indenter = new DefaultIndenter("  ", "\n");
            printer.indentArraysWith(indenter);
            printer.indentObjectsWith(indenter);
            writer = mapper.writer(printer);
        }

        public String writeAsString(Object value)
                throws JsonProcessingException {
            return writer.writeValueAsString(value);
        }
    }
}
