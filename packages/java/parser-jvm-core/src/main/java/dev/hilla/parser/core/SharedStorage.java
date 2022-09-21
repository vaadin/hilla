package dev.hilla.parser.core;

import io.swagger.v3.oas.models.OpenAPI;

public final class SharedStorage {
    private final ParserConfig parserConfig;

    SharedStorage(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }

    public OpenAPI getOpenAPI() {
        return parserConfig.getOpenAPI();
    }

    public ParserConfig getParserConfig() {
        return parserConfig;
    }
}
