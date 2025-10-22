package com.vaadin.hilla.parser.core;

import io.swagger.v3.oas.models.OpenAPI;

public final class SharedStorage {
    private final Parser.Config parserConfig;

    SharedStorage(Parser.Config parserConfig) {
        this.parserConfig = parserConfig;
    }

    public OpenAPI getOpenAPI() {
        return parserConfig.getOpenAPI();
    }

    public Parser.Config getParserConfig() {
        return parserConfig;
    }
}
