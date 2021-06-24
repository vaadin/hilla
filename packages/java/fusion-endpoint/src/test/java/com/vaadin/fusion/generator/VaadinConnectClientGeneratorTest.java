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
package com.vaadin.fusion.generator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class VaadinConnectClientGeneratorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path outputPath;

    @Before
    public void setUpOutputFile() {
        outputPath = Paths.get(temporaryFolder.getRoot().getAbsolutePath(),
                VaadinConnectClientGenerator.CONNECT_CLIENT_NAME);
    }

    @Test
    public void should_GenerateConnectClientDefault_When_NoApplicationPropertiesInput()
            throws Exception {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                new Properties());

        generator.generateVaadinConnectClientFile(outputPath);

        Assert.assertTrue(outputPath.toFile().exists());
        String actualJson = StringUtils.toEncodedString(
                Files.readAllBytes(outputPath), StandardCharsets.UTF_8).trim();
        String expectedJson = TestUtils.readResource(
                getClass().getResource("expected-connect-client-default.ts"));
        Assert.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void should_GenerateConnectClientDefault_When_ApplicationPropertiesInput()
            throws Exception {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                TestUtils.readProperties(getClass()
                        .getResource("application.properties.for.testing")
                        .getPath()));

        generator.generateVaadinConnectClientFile(outputPath);

        Assert.assertTrue(outputPath.toFile().exists());
        String actualJson = StringUtils.toEncodedString(
                Files.readAllBytes(outputPath), StandardCharsets.UTF_8).trim();
        String expectedJson = TestUtils.readResource(
                getClass().getResource("expected-connect-client-custom.ts"));
        Assert.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_work_with_both_default_values() {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                VaadinConnectClientGenerator.DEFAULT_PREFIX, "/*");
        Assert.assertEquals("connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_work_with_custom_url_mapping_and_default_endpoint_prefix() {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                VaadinConnectClientGenerator.DEFAULT_PREFIX, "/myapp/*");
        Assert.assertEquals("../connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_work_with_default_url_mapping_and_custom_endpoint_prefix() {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                new Properties());
        String result = generator
                .relativizeEndpointPrefixWithUrlMapping("/my-connect", "/*");
        Assert.assertEquals("my-connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_work_with_custom_url_mapping_and_custom_endpoint_prefix() {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                "/my-connect", "/myapp/*");
        Assert.assertEquals("../my-connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_work_with_multiple_level_url_mapping_and_custom_endpoint_prefix() {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                "/my-connect", "/myapp/yourapp/*");
        Assert.assertEquals("../../my-connect", result);
    }

    @Test
    public void relativizeEndpointPrefixWithUrlMapping_should_work_with_multiple_level_url_mapping_and_multiple_level_endpoint_prefix() {
        VaadinConnectClientGenerator generator = new VaadinConnectClientGenerator(
                new Properties());
        String result = generator.relativizeEndpointPrefixWithUrlMapping(
                "/my-connect/your-connect", "/myapp/yourapp/*");
        Assert.assertEquals("../../my-connect/your-connect", result);
    }
}
