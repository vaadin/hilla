package com.vaadin.hilla.parser.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
