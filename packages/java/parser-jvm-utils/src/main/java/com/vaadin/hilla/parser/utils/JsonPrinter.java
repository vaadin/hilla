package com.vaadin.hilla.parser.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.models.OpenAPI;
import tools.jackson.core.JacksonException;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

public final class JsonPrinter {
    private final ObjectMapper mapper = Json.mapper();
    private final Pretty pretty;

    public JsonPrinter() {
        mapper.findAndRegisterModules();
        // Putting the `pretty` initialization here allows preserving correct
        // class initialization sequence.
        pretty = new Pretty();
    }

    public Pretty pretty() {
        return pretty;
    }

    public String writeAsString(Object value) throws JacksonException {
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

        public String writeAsString(Object value) throws JacksonException {
            return writer.writeValueAsString(value);
        }
    }
}
