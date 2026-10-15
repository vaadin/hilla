/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

/**
 * This test suite is only for triggering the OpenAPI generator. For the actual
 * content of the generator, they are tested in other package
 * {@link com.vaadin.hilla.generator}
 */
public class TaskGenerateOpenAPITest extends TaskTest {

    private TaskGenerateOpenAPI taskGenerateOpenApi;

    @Test
    public void should_UseCustomEndpointNameWithoutValueEqual_InsteadOf_UsingClassName()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenAPIImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile(),
                getClass()::getResource, getClass().getClassLoader(), false);

        taskGenerateOpenApi.execute();

        var generatedOpenAPI = getGeneratedOpenAPI();

        assertThat(generatedOpenAPI.getPaths(),
                hasKey("/WithoutValueEqual/bar"));
        assertThat(generatedOpenAPI.getPaths(),
                not(hasKey("/EndpointNoValue/bar")));
        assertThat(generatedOpenAPI.getPaths(),
                hasKey("/WithoutValueEqual/foo"));
        assertThat(generatedOpenAPI.getPaths(),
                not(hasKey("/EndpointNoValue/foo")));
    }

    @Test
    public void should_UseCustomEndpointName_InsteadOf_UsingClassName()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenAPIImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile(),
                getClass()::getResource, getClass().getClassLoader(), false);

        taskGenerateOpenApi.execute();

        var generatedOpenAPI = getGeneratedOpenAPI();

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
    public void should_UseDefaultProperties_when_applicationPropertiesIsEmpty()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenAPIImpl(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile(),
                getClass()::getResource, getClass().getClassLoader(), false);

        taskGenerateOpenApi.execute();

        var generatedOpenAPI = getGeneratedOpenAPI();
        var info = generatedOpenAPI.getInfo();
        assertEquals("Hilla Application", info.getTitle(),
                "Generated OpenAPI should have default application title");

        assertEquals("1.0.0", info.getVersion(),
                "Generated OpenAPI should have default application API version");

        var servers = generatedOpenAPI.getServers();
        assertEquals(1, servers.size(),
                "Generated OpenAPI should a default server");
        assertEquals("http://localhost:8080/connect", servers.get(0).getUrl(),
                "Generated OpenAPI should have default url server");

        assertEquals("Hilla Backend", servers.get(0).getDescription(),
                "Generated OpenAPI should have default server description");
    }

    private OpenAPI getGeneratedOpenAPI() {
        return new OpenAPIV3Parser()
                .read(getOpenAPIFile().toFile().getAbsolutePath());
    }
}
