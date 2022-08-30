package dev.hilla.parser.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;

public final class SharedStorage {
    private final AssociationMap associationMap = new AssociationMap();
    private final ClassMappers classMappers = new ClassMappers();
    private final ParserConfig parserConfig;
    private final Map<String, Object> pluginStorage = new HashMap<>();
    private final ScanResult scanResult;

    SharedStorage(ParserConfig parserConfig, ScanResult scanResult) {
        this.parserConfig = parserConfig;
        this.scanResult = scanResult;
    }

    public AssociationMap getAssociationMap() {
        return associationMap;
    }

    public ClassMappers getClassMappers() {
        return classMappers;
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

    public ScanResult getScanResult() {
        return scanResult;
    }

    public String getEndpointAnnotationName() {
        return parserConfig.getEndpointAnnotationName();
    }

    public String getEndpointExposedAnnotationName() {
        return parserConfig.getEndpointExposedAnnotationName();
    }
}
