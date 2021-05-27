/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.server.frontend.fusion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaadin.flow.server.frontend.TaskGenerateOpenApi;
import com.vaadin.flow.server.connect.generator.OpenApiSpecGenerator;

/**
 * This test suite is only for triggering the OpenAPI generator. For the actual
 * content of the generator, they are tested in other package
 * {@link com.vaadin.flow.server.connect.generator}
 */
public class TaskGenerateOpenApiTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File applicationPropertiesFile;
    private TaskGenerateOpenApi taskGenerateOpenApi;
    private File generatedOpenAPI;
    private File javaSource;

    @Before
    public void setUp() throws IOException {
        applicationPropertiesFile = temporaryFolder
                .newFile("application.properties");
        generatedOpenAPI = new File(temporaryFolder.newFolder(),
                "generated-openapi.json");
        javaSource = new File(
                getClass().getClassLoader().getResource("java").getFile());
    }

    @Test
    public void should_UseDefaultProperties_when_applicationPropertiesIsEmpty()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenApiImpl(
                applicationPropertiesFile, javaSource,
                this.getClass().getClassLoader(), generatedOpenAPI);

        taskGenerateOpenApi.execute();

        OpenAPI generatedOpenAPI = getGeneratedOpenAPI();
        Info info = generatedOpenAPI.getInfo();
        Assert.assertEquals(
                "Generated OpenAPI should have default application title",
                OpenApiSpecGenerator.DEFAULT_APPLICATION_TITLE,
                info.getTitle());
        Assert.assertEquals(
                "Generated OpenAPI should have default "
                        + "application API version",
                OpenApiSpecGenerator.DEFAULT_APPLICATION_API_VERSION,
                info.getVersion());

        List<Server> servers = generatedOpenAPI.getServers();
        Assert.assertEquals("Generated OpenAPI should a default server", 1,
                servers.size());
        Assert.assertEquals("Generated OpenAPI should have default url server",
                OpenApiSpecGenerator.DEFAULT_SERVER
                        + OpenApiSpecGenerator.DEFAULT_PREFIX,
                servers.get(0).getUrl());

        Assert.assertEquals(
                "Generated OpenAPI should have default server description",
                OpenApiSpecGenerator.DEFAULT_SERVER_DESCRIPTION,
                servers.get(0).getDescription());
    }

    @Test
    public void should_UseGivenProperties_when_applicationPropertiesDefinesThem()
            throws Exception {

        String applicationTitle = "My title";
        String applicationAPIVersion = "1.1.1";
        String applicationServer = "https://example.com";
        String applicationPrefix = "/api";
        String applicationServerDescription = "Example API server";
        StringBuilder applicationPropertiesBuilder = new StringBuilder();
        applicationPropertiesBuilder
                .append(OpenApiSpecGenerator.APPLICATION_TITLE).append("=")
                .append(applicationTitle).append("\n")
                .append(OpenApiSpecGenerator.APPLICATION_API_VERSION)
                .append("=").append(applicationAPIVersion).append("\n")
                .append(OpenApiSpecGenerator.SERVER).append("=")
                .append(applicationServer).append("\n")
                .append(OpenApiSpecGenerator.PREFIX).append("=")
                .append(applicationPrefix).append("\n")
                .append(OpenApiSpecGenerator.SERVER_DESCRIPTION).append("=")
                .append(applicationServerDescription);
        FileUtils.writeStringToFile(applicationPropertiesFile,
                applicationPropertiesBuilder.toString(),
                StandardCharsets.UTF_8);

        taskGenerateOpenApi = new TaskGenerateOpenApiImpl(
                applicationPropertiesFile, javaSource,
                this.getClass().getClassLoader(), generatedOpenAPI);
        taskGenerateOpenApi.execute();

        OpenAPI generatedOpenAPI = getGeneratedOpenAPI();
        Info info = generatedOpenAPI.getInfo();
        Assert.assertEquals(
                "Generated OpenAPI should use given application title",
                applicationTitle, info.getTitle());
        Assert.assertEquals(
                "Generated OpenAPI should use given"
                        + "application API version",
                applicationAPIVersion, info.getVersion());

        List<Server> servers = generatedOpenAPI.getServers();
        Assert.assertEquals("Generated OpenAPI should a defined server", 1,
                servers.size());
        Assert.assertEquals("Generated OpenAPI should use given url server",
                applicationServer + applicationPrefix, servers.get(0).getUrl());

        Assert.assertEquals(
                "Generated OpenAPI should use given server description",
                applicationServerDescription, servers.get(0).getDescription());
    }

    @Test
    public void should_UseCustomEndpointName_InsteadOf_UsingClassName()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenApiImpl(
                applicationPropertiesFile, javaSource,
                this.getClass().getClassLoader(), generatedOpenAPI);
        taskGenerateOpenApi.execute();

        OpenAPI generatedOpenAPI = getGeneratedOpenAPI();

        Assert.assertTrue(generatedOpenAPI.getPaths()
                .containsKey("/CustomEndpointName/bar"));
        Assert.assertFalse(
                generatedOpenAPI.getPaths().containsKey("/CustomEndpoint/bar"));
        Assert.assertTrue(generatedOpenAPI.getPaths()
                .containsKey("/CustomEndpointName/foo"));
        Assert.assertFalse(
                generatedOpenAPI.getPaths().containsKey("/CustomEndpoint/foo"));
    }

    @Test
    public void should_UseCustomEndpointNameWithoutValueEqual_InsteadOf_UsingClassName()
            throws Exception {
        taskGenerateOpenApi = new TaskGenerateOpenApiImpl(
                applicationPropertiesFile, javaSource,
                this.getClass().getClassLoader(), generatedOpenAPI);
        taskGenerateOpenApi.execute();

        OpenAPI generatedOpenAPI = getGeneratedOpenAPI();

        Assert.assertTrue(generatedOpenAPI.getPaths()
                .containsKey("/WithoutValueEqual/bar"));
        Assert.assertFalse(generatedOpenAPI.getPaths()
                .containsKey("/EndpointNoValue/bar"));
        Assert.assertTrue(generatedOpenAPI.getPaths()
                .containsKey("/WithoutValueEqual/foo"));
        Assert.assertFalse(generatedOpenAPI.getPaths()
                .containsKey("/EndpointNoValue/foo"));
    }

    private OpenAPI getGeneratedOpenAPI() throws IOException {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        return parser.read(generatedOpenAPI.getAbsolutePath());
    }

}
