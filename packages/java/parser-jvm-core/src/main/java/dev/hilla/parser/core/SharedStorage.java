package dev.hilla.parser.core;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.models.OpenAPI;

public final class SharedStorage {
    private final AssociationMap associationMap = new AssociationMap();
    private final MappingRuleSet mappingRuleSet = new MappingRuleSet();
    private final ParserConfig parserConfig;
    private final Map<String, Object> pluginStorage = new HashMap<>();

    SharedStorage(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public MappingRuleSet getMappingRuleSet() {
        return mappingRuleSet;
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
