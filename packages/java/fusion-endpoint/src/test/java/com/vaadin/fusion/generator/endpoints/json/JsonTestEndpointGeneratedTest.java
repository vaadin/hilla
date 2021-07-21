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

package com.vaadin.fusion.generator.endpoints.json;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import com.fasterxml.jackson.core.Version;
import org.junit.Test;

import com.vaadin.fusion.generator.MainGenerator;
import com.vaadin.fusion.generator.OpenAPISpecGenerator;
import com.vaadin.fusion.generator.ClientAPIGenerator;
import com.vaadin.fusion.generator.endpoints.AbstractEndpointGenerationTest;
import com.vaadin.fusion.utils.TestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonTestEndpointGeneratedTest
        extends AbstractEndpointGenerationTest {

    public JsonTestEndpointGeneratedTest() {
        super(Arrays.asList(JsonTestEndpoint.class, Version.class));
    }

    @Test
    public void should_GenerateJsClassWithCustomClientPath_When_CustomClientPathGiven() {
        String customConnectClientPath = "../my-connect-client.js";
        String expectedImport = String.format("import client from '%s';",
                customConnectClientPath);

        new OpenAPISpecGenerator(new Properties()).generateOpenApiSpec(
                TestUtils.getClassFilePath(getClass().getPackage()),
                openApiJsonOutput);

        new MainGenerator(openApiJsonOutput.toFile(), outputDirectory.getRoot(),
                customConnectClientPath).start();

        getTsFiles(outputDirectory.getRoot()).stream().map(File::toPath)
                .map(this::readFile).forEach(fileContents -> assertTrue(
                        fileContents.contains(expectedImport)));
    }

    @Test
    public void should_GenerateJsClass_When_ThereIsOpenApiInputAndNoTargetDirectory() {
        File nonExistingOutputDirectory = new File(outputDirectory.getRoot(),
                "whatever");
        assertFalse(nonExistingOutputDirectory.isDirectory());

        File openApiFile = new File(getClass()
                .getResource(
                        "expected-openapi-custom-application-properties.json")
                .getPath());

        new MainGenerator(openApiFile, nonExistingOutputDirectory).start();

        assertTrue(nonExistingOutputDirectory.isDirectory());
        assertFalse(getTsFiles(nonExistingOutputDirectory).isEmpty());
    }

    @Test
    public void should_GenerateOpenApiWithCustomApplicationProperties_When_InputApplicationPropertiesGiven() {
        verifyGenerationFully(
                AbstractEndpointGenerationTest.class
                        .getResource("../application.properties.for.testing"),
                getClass().getResource(
                        "expected-openapi-custom-application-properties.json"));
    }

    @Test
    public void should_GenerateOpenApi_When_NoApplicationPropertiesInput() {
        String expectedImport = String.format("import client from '%s';",
                ClientAPIGenerator.CONNECT_CLIENT_IMPORT_PATH);
        verifyGenerationFully(null,
                getClass().getResource("expected-openapi.json"));

        getTsFiles(outputDirectory.getRoot()).stream().map(File::toPath)
                .map(this::readFile).forEach(fileContents -> assertTrue(
                        fileContents.contains(expectedImport)));
    }

}
