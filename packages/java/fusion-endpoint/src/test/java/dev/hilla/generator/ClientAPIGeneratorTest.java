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
package dev.hilla.generator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import dev.hilla.utils.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ClientAPIGeneratorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_Should_workWithBothDefaultValues() {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(), new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                ClientAPIGenerator.DEFAULT_PREFIX, "/*");
        Assert.assertEquals("connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_workWithCustomUrlMappingAndCustomEndpointPrefix() {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(), new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                "/my-connect", "/myapp/*");
        Assert.assertEquals("../my-connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_workWithCustomUrlMappingAndDefaultEndpointPrefix() {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(), new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                ClientAPIGenerator.DEFAULT_PREFIX, "/myapp/*");
        Assert.assertEquals("../connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_workWithDefaultUrlMappingAndCustomEndpointPrefix() {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(), new Properties());
        String result = generator
                .relativizeEndpointPrefixWithUrlMapping("/my-connect", "/*");
        Assert.assertEquals("my-connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_Should_WorkWithMultipleLevelUrlMappingAndCustomEndpointPrefix() {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(), new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                "/my-connect", "/myapp/yourapp/*");
        Assert.assertEquals("../../my-connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_Should_WorkWithMultipleLevelUrlMappingAndMultipleLevelEndpointPrefix() {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(), new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                "/my-connect/your-connect", "/myapp/yourapp/*");
        Assert.assertEquals("../../my-connect/your-connect", result);
    }

    @Test
    public void should_GenerateConnectClientDefault_When_ApplicationPropertiesInput()
            throws Exception {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(),
                TestUtils.readProperties(getClass()
                        .getResource("application.properties.for.testing")
                        .getPath()));

        generator.generate();

        Path outputPath = generator.getOutputFilePath();

        Assert.assertTrue(outputPath.toFile().exists());
        String actualJson = StringUtils.toEncodedString(
                Files.readAllBytes(outputPath), StandardCharsets.UTF_8).trim();
        String expectedJson = TestUtils.readResource(
                getClass().getResource("expected-connect-client-custom.ts"));
        Assert.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void should_GenerateConnectClientDefault_When_NoApplicationPropertiesInput()
            throws Exception {
        ClientAPIGenerator generator = new ClientAPIGenerator(
                temporaryFolder.getRoot().toPath(), new Properties());

        generator.generate();

        Path outputPath = generator.getOutputFilePath();

        Assert.assertTrue(outputPath.toFile().exists());
        String actualJson = StringUtils.toEncodedString(
                Files.readAllBytes(outputPath), StandardCharsets.UTF_8).trim();
        String expectedJson = TestUtils.readResource(
                getClass().getResource("expected-connect-client-default.ts"));
        Assert.assertEquals(expectedJson, actualJson);
    }
}
