/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

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
