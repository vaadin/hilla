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
package com.vaadin.hilla.parser.plugins.transfertypes.test.helpers;

import static com.vaadin.hilla.parser.testutils.JsonAssertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.core.util.Json;
import com.vaadin.hilla.parser.testutils.ResourceLoader;

import io.swagger.v3.oas.models.OpenAPI;

public final class TestHelper {
    private final ResourceLoader resourceLoader;
    private final Path targetDir;

    public TestHelper(Class<?> cls) {
        try {
            this.resourceLoader = new ResourceLoader(cls);
            this.targetDir = resourceLoader.findTargetDirPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeParserWithConfig(OpenAPI openAPI)
            throws IOException, URISyntaxException {
        var mapper = Json.mapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);

        assertEquals(expected, openAPI);
    }

    public String getExtendedClassPath(Class<?>... classes)
            throws URISyntaxException {
        return ResourceLoader.getClasspath(Arrays.stream(classes)
                .map(ResourceLoader::new).collect(Collectors.toList()));
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
