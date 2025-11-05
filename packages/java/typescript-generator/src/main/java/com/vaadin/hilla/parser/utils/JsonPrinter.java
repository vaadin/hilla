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
    private final ObjectMapper mapper = new ObjectMapper();
    private final com.fasterxml.jackson.databind.ObjectMapper jacksonV2Mapper;
    private final Pretty pretty;

    public JsonPrinter() {
        // Jackson 3 has JavaTimeModule, Jdk8Module, ParameterNamesModule
        // built-in
        // Putting the `pretty` initialization here allows preserving correct
        // class initialization sequence.

        // Use Swagger's Jackson 2 mapper configured to match test format
        jacksonV2Mapper = io.swagger.v3.core.util.Json.mapper();
        jacksonV2Mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Don't sort map entries or properties alphabetically to preserve
        // method parameter order
        jacksonV2Mapper.configure(
                SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
        jacksonV2Mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,
                false);

        pretty = new Pretty();
    }

    public Pretty pretty() {
        return pretty;
    }

    public String writeAsString(Object value) throws JacksonException {
        // Use Jackson 2 for OpenAPI objects to match parser configuration
        if (value instanceof OpenAPI) {
            try {
                return jacksonV2Mapper.writeValueAsString(value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize OpenAPI object",
                        e);
            }
        }
        return mapper.writeValueAsString(value);
    }

    public final class Pretty {
        private final ObjectWriter writer;
        private final com.fasterxml.jackson.databind.ObjectWriter jacksonV2Writer;

        private Pretty() {
            var printer = new DefaultPrettyPrinter();
            var indenter = new DefaultIndenter("  ", "\n");
            printer.indentArraysWith(indenter);
            printer.indentObjectsWith(indenter);
            writer = mapper.writer().with(printer);

            // Configure Jackson 2 pretty printer for OpenAPI objects
            var j2Printer = new com.fasterxml.jackson.core.util.DefaultPrettyPrinter();
            var j2Indenter = new com.fasterxml.jackson.core.util.DefaultIndenter(
                    "  ", "\n");
            j2Printer.indentArraysWith(j2Indenter);
            j2Printer.indentObjectsWith(j2Indenter);
            jacksonV2Writer = jacksonV2Mapper.writer().with(j2Printer);
        }

        public String writeAsString(Object value) throws JacksonException {
            // Use Jackson 2 for OpenAPI objects to match parser configuration
            if (value instanceof OpenAPI) {
                try {
                    return jacksonV2Writer.writeValueAsString(value);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to serialize OpenAPI object", e);
                }
            }
            return writer.writeValueAsString(value);
        }
    }
}
