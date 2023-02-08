package dev.hilla.parser.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;

public enum OpenAPIFileType {
    JSON(Json.mapper()), YAML(Yaml.mapper());

    private final ObjectMapper mapper;

    OpenAPIFileType(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
