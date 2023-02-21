/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package dev.hilla.internal;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test suite is only for triggering the OpenAPI generator. For the actual
 * content of the generator, they are tested in other package
 * {@link dev.hilla.generator}
 */
public class TaskGenerateOpenAPITest extends TaskTest {

    private TaskGenerateOpenAPI taskGenerateOpenApi;

    @Test
    public void should_UseDefaultProperties_when_applicationPropertiesIsEmpty()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenAPIImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile(),
                getClass().getClassLoader());

        taskGenerateOpenApi.execute();

        OpenAPI generatedOpenAPI = getGeneratedOpenAPI();
        Info info = generatedOpenAPI.getInfo();
        assertEquals("Hilla Application", info.getTitle(),
                "Generated OpenAPI should have default application title");

        assertEquals("1.0.0", info.getVersion(),
                "Generated OpenAPI should have default application API version");

        List<Server> servers = generatedOpenAPI.getServers();
        assertEquals(1, servers.size(),
                "Generated OpenAPI should a default server");
        assertEquals("http://localhost:8080/connect", servers.get(0).getUrl(),
                "Generated OpenAPI should have default url server");

        assertEquals("Hilla Backend", servers.get(0).getDescription(),
                "Generated OpenAPI should have default server description");
    }

    @Test
    public void should_UseCustomEndpointName_InsteadOf_UsingClassName()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenAPIImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile(),
                getClass().getClassLoader());

        taskGenerateOpenApi.execute();

        OpenAPI generatedOpenAPI = getGeneratedOpenAPI();

        assertThat(generatedOpenAPI.getPaths(),
                hasKey("/CustomEndpointName/bar"));
        assertThat(generatedOpenAPI.getPaths(),
                not(hasKey("/CustomEndpoint/bar")));
        assertThat(generatedOpenAPI.getPaths(),
                hasKey("/CustomEndpointName/foo"));
        assertThat(generatedOpenAPI.getPaths(),
                not(hasKey("/CustomEndpoint/foo")));
    }

    @Test
    public void should_UseCustomEndpointNameWithoutValueEqual_InsteadOf_UsingClassName()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenAPIImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile(),
                getClass().getClassLoader());

        taskGenerateOpenApi.execute();

        OpenAPI generatedOpenAPI = getGeneratedOpenAPI();

        assertThat(generatedOpenAPI.getPaths(),
                hasKey("/WithoutValueEqual/bar"));
        assertThat(generatedOpenAPI.getPaths(),
                not(hasKey("/EndpointNoValue/bar")));
        assertThat(generatedOpenAPI.getPaths(),
                hasKey("/WithoutValueEqual/foo"));
        assertThat(generatedOpenAPI.getPaths(),
                not(hasKey("/EndpointNoValue/foo")));
    }

    private OpenAPI getGeneratedOpenAPI() {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        return parser.read(getOpenAPIFile().toFile().getAbsolutePath());
    }
}
