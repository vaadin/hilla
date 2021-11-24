package com.vaadin.fusion.parser.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public final class OpenAPIPrinter {
    private final ObjectMapper mapper = Json.mapper();
    private final Pretty pretty;

    public OpenAPIPrinter() {
        // Putting the `pretty` initialization here allows preserving correct
        // class initialization sequence.
        pretty = new Pretty();
    }

    public String writeAsString(OpenAPI value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }

    public Pretty pretty() {
        return pretty;
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

        public String writeAsString(OpenAPI value)
                throws JsonProcessingException {
            return writer.writeValueAsString(value);
        }
    }
}
