package com.vaadin.hilla.parser.utils;

import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

public final class JsonPrinter {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Pretty pretty;

    public JsonPrinter() {
        // Jackson 3.x modules are automatically registered
        // Putting the `pretty` initialization here allows preserving correct
        // class initialization sequence.
        pretty = new Pretty();
    }

    public Pretty pretty() {
        return pretty;
    }

    public String writeAsString(Object value) {
        return mapper.writeValueAsString(value);
    }

    public final class Pretty {
        private final ObjectWriter writer;

        private Pretty() {
            var printer = new DefaultPrettyPrinter();
            var indenter = new DefaultIndenter("  ", "\n");
            printer.indentArraysWith(indenter);
            printer.indentObjectsWith(indenter);
            writer = mapper.writerWithDefaultPrettyPrinter().with(printer);
        }

        public String writeAsString(Object value) {
            return writer.writeValueAsString(value);
        }
    }
}
