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
package com.vaadin.hilla.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class HillaFeaturePropertiesTest {

    @TempDir
    Path tempDir;

    // ---- defaults and missing file ----

    @Test
    public void defaultsShouldHaveAllFeaturesEnabled() {
        var defaults = HillaFeatureProperties.defaults();
        assertTrue(defaults.isFileRouterEnabled());
        assertTrue(defaults.isAutoCrudEnabled());
        assertTrue(defaults.isVaadinUiEnabled());
    }

    @Test
    public void shouldReturnDefaultsWhenNoConfigFile() {
        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    // ---- .properties file ----

    @Test
    public void shouldReturnDefaultsWhenEmptyPropertiesFile()
            throws IOException {
        writeFile("application.properties", "");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadFileRouterDisabled() throws IOException {
        writeFile("application.properties",
                "hilla.file-router.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadAutoCrudDisabled() throws IOException {
        writeFile("application.properties", "hilla.auto-crud.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertFalse(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadVaadinUiDisabled() throws IOException {
        writeFile("application.properties", "hilla.vaadin-ui.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertFalse(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadAllDisabled() throws IOException {
        writeFile("application.properties", """
                hilla.file-router.enabled=false
                hilla.auto-crud.enabled=false
                hilla.vaadin-ui.enabled=false
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertFalse(props.isAutoCrudEnabled());
        assertFalse(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadExplicitlyEnabledFeatures() throws IOException {
        writeFile("application.properties", """
                hilla.file-router.enabled=true
                hilla.auto-crud.enabled=true
                hilla.vaadin-ui.enabled=true
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldHandleMixedProperties() throws IOException {
        writeFile("application.properties", """
                hilla.file-router.enabled=false
                hilla.auto-crud.enabled=true
                hilla.vaadin-ui.enabled=false
                some.other.property=value
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertFalse(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldDefaultMissingPropertiesToTrue() throws IOException {
        writeFile("application.properties", "hilla.file-router.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    // ---- .yml file ----

    @Test
    public void shouldReadYmlFileWithAllDisabled() throws IOException {
        writeFile("application.yml", """
                hilla:
                  file-router:
                    enabled: false
                  auto-crud:
                    enabled: false
                  vaadin-ui:
                    enabled: false
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertFalse(props.isAutoCrudEnabled());
        assertFalse(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadYmlFileWithSingleDisabled() throws IOException {
        writeFile("application.yml", """
                hilla:
                  auto-crud:
                    enabled: false
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertFalse(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadYmlFileWithExplicitlyEnabled() throws IOException {
        writeFile("application.yml", """
                hilla:
                  file-router:
                    enabled: true
                  auto-crud:
                    enabled: true
                  vaadin-ui:
                    enabled: true
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReturnDefaultsForEmptyYml() throws IOException {
        writeFile("application.yml", "");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReturnDefaultsForYmlWithoutHillaSection()
            throws IOException {
        writeFile("application.yml", """
                spring:
                  datasource:
                    url: jdbc:h2:mem:test
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldHandleYmlWithMixedContent() throws IOException {
        writeFile("application.yml", """
                spring:
                  datasource:
                    url: jdbc:h2:mem:test
                hilla:
                  file-router:
                    enabled: false
                  vaadin-ui:
                    enabled: false
                server:
                  port: 8080
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertFalse(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldHandleYmlStringValues() throws IOException {
        writeFile("application.yml", """
                hilla:
                  file-router:
                    enabled: "false"
                  auto-crud:
                    enabled: "true"
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    // ---- .yaml file ----

    @Test
    public void shouldReadYamlExtension() throws IOException {
        writeFile("application.yaml", """
                hilla:
                  file-router:
                    enabled: false
                  auto-crud:
                    enabled: false
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertFalse(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    // ---- precedence ----

    @Test
    public void propertiesFileShouldTakePrecedenceOverYml()
            throws IOException {
        writeFile("application.properties",
                "hilla.file-router.enabled=false");
        writeFile("application.yml", """
                hilla:
                  file-router:
                    enabled: true
                  auto-crud:
                    enabled: false
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        // .properties wins: file-router is false
        assertFalse(props.isFileRouterEnabled());
        // .yml is ignored entirely, so auto-crud defaults to true
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void ymlShouldTakePrecedenceOverYaml() throws IOException {
        writeFile("application.yml", """
                hilla:
                  auto-crud:
                    enabled: false
                """);
        writeFile("application.yaml", """
                hilla:
                  auto-crud:
                    enabled: true
                  file-router:
                    enabled: false
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        // .yml wins: auto-crud is false
        assertFalse(props.isAutoCrudEnabled());
        // .yaml is ignored, so file-router defaults to true
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void yamlShouldBeUsedWhenNoPropertiesOrYml() throws IOException {
        writeFile("application.yaml", """
                hilla:
                  vaadin-ui:
                    enabled: false
                """);

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertFalse(props.isVaadinUiEnabled());
    }

    // ---- builder integration ----

    @Test
    public void builderShouldRetainFeatureProperties() {
        var features = new HillaFeatureProperties(false, false, false);
        var config = new EngineAutoConfiguration.Builder()
                .featureProperties(features).build();
        assertEquals(features, config.getFeatureProperties());
        assertFalse(config.getFeatureProperties().isFileRouterEnabled());
        assertFalse(config.getFeatureProperties().isAutoCrudEnabled());
        assertFalse(config.getFeatureProperties().isVaadinUiEnabled());
    }

    // ---- helper ----

    private void writeFile(String fileName, String content)
            throws IOException {
        var resourcesDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);
        Files.writeString(resourcesDir.resolve(fileName), content);
    }
}
