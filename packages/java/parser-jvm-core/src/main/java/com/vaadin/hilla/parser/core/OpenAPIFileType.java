package com.vaadin.hilla.parser.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public enum OpenAPIFileType {
    // Use custom configured Jackson 2 mappers to match test expectations
    JSON(createJsonMapper()), YAML(createYamlMapper());

    private final ObjectMapper mapper;

    OpenAPIFileType(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private static ObjectMapper createJsonMapper() {
        // Use Swagger's mapper but configure to match compact test format
        ObjectMapper mapper = io.swagger.v3.core.util.Json.mapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private static ObjectMapper createYamlMapper() {
        // Use Swagger's YAML mapper but configure to match compact format
        ObjectMapper mapper = io.swagger.v3.core.util.Yaml.mapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
