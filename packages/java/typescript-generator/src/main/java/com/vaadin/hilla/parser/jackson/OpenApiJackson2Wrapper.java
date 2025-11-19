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
package com.vaadin.hilla.parser.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Wrapper for handling OpenAPI/Swagger models with Jackson 2. This is needed
 * because Swagger models have Jackson 2 annotations, which are incompatible
 * with Jackson 3.
 *
 * We use Swagger's Json31.mapper() which has all the proper configurations for
 * handling OpenAPI 3.1 models, including the custom deserializers for fields
 * like additionalProperties.
 */
public class OpenApiJackson2Wrapper {
    // Use Swagger's own 3.1 mapper which has the proper configuration
    private static final ObjectMapper jackson2Mapper = Json31.mapper();

    /**
     * Deserialize OpenAPI model using Jackson 2.
     */
    public static OpenAPI readValue(byte[] content) {
        try {
            return jackson2Mapper.readValue(content, OpenAPI.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize OpenAPI model",
                    e);
        }
    }

    /**
     * Deserialize OpenAPI model using Jackson 2.
     */
    public static OpenAPI readValue(String content) {
        try {
            return jackson2Mapper.readValue(content, OpenAPI.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize OpenAPI model",
                    e);
        }
    }

    /**
     * Serialize OpenAPI model using Jackson 2.
     */
    public static String writeValueAsString(OpenAPI openAPI) {
        try {
            return jackson2Mapper.writeValueAsString(openAPI);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OpenAPI model", e);
        }
    }
}
