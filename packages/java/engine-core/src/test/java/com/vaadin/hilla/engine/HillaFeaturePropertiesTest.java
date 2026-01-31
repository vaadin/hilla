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

    @Test
    public void defaultsShouldHaveAllFeaturesEnabled() {
        var defaults = HillaFeatureProperties.defaults();
        assertTrue(defaults.isFileRouterEnabled());
        assertTrue(defaults.isAutoCrudEnabled());
        assertTrue(defaults.isVaadinUiEnabled());
    }

    @Test
    public void shouldReturnDefaultsWhenNoPropertiesFile() {
        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReturnDefaultsWhenEmptyPropertiesFile()
            throws IOException {
        var propsDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(propsDir);
        Files.writeString(propsDir.resolve("application.properties"), "");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadFileRouterDisabled() throws IOException {
        writeProperties("hilla.file-router.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadAutoCrudDisabled() throws IOException {
        writeProperties("hilla.auto-crud.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertFalse(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadVaadinUiDisabled() throws IOException {
        writeProperties("hilla.vaadin-ui.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertTrue(props.isFileRouterEnabled());
        assertTrue(props.isAutoCrudEnabled());
        assertFalse(props.isVaadinUiEnabled());
    }

    @Test
    public void shouldReadAllDisabled() throws IOException {
        writeProperties("""
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
        writeProperties("""
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
        writeProperties("""
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
        writeProperties("hilla.file-router.enabled=false");

        var props = HillaFeatureProperties.fromBaseDir(tempDir);
        assertFalse(props.isFileRouterEnabled());
        // Not specified in properties file, should default to true
        assertTrue(props.isAutoCrudEnabled());
        assertTrue(props.isVaadinUiEnabled());
    }

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

    private void writeProperties(String content) throws IOException {
        var propsDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(propsDir);
        Files.writeString(propsDir.resolve("application.properties"), content);
    }
}
