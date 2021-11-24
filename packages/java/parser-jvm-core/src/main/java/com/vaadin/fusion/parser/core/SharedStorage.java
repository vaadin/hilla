package com.vaadin.fusion.parser.core;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.models.OpenAPI;

public final class SharedStorage {
    private final ParserConfig parserConfig;
    private final Map<String, Object> pluginStorage = new HashMap<>();

    SharedStorage(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }

    public OpenAPI getOpenAPI() {
        return parserConfig.getOpenAPI();
    }

    public ParserConfig getParserConfig() {
        return parserConfig;
    }

    public Map<String, Object> getPluginStorage() {
        return pluginStorage;
    }
}
