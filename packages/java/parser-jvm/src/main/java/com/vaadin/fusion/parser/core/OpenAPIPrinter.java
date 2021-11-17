package com.vaadin.fusion.parser.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenAPIPrinter {
    private final ObjectMapper mapper = Json.mapper();
    private final Pretty pretty;

    public OpenAPIPrinter() {
        pretty = new Pretty();
    }

    public String writeAsString(OpenAPI value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }

    public Pretty pretty() {
        return pretty;
    }

    public class Pretty {
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

    @JsonIgnoreProperties({ "exampleSetFlag" })
    private static abstract class ExampleSetFlagRemoveMixIn {
    }

    private static class OpenAPIMixInResolver
            implements ClassIntrospector.MixInResolver {
        @Override
        public Class<?> findMixInClassFor(Class<?> cls) {
            return ExampleSetFlagRemoveMixIn.class;
        }

        @Override
        public ClassIntrospector.MixInResolver copy() {
            return this;
        }
    }
}
