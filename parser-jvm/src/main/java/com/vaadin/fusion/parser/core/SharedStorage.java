package com.vaadin.fusion.parser.core;

import java.util.HashMap;
import java.util.Map;

import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public class SharedStorage {
    private final ParserConfig parserConfig;
    private final OpenApi3 openAPI;
    private final Map<String, Object> pluginStorage = new HashMap<>();

    SharedStorage(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;

        try {
            openAPI = new OpenApi3Parser()
                    .parse(getClass().getResource("OpenAPIBase.json"));
        } catch (Exception e) {
            throw new ParserException("Cannot create SharedStorage", e);
        }
    }

    public OpenApi3 getOpenAPI() {
        return openAPI;
    }

    public ParserConfig getParserConfig() {
        return parserConfig;
    }

    public Map<String, Object> getPluginStorage() {
        return pluginStorage;
    }
}
