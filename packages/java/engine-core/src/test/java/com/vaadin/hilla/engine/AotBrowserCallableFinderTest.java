/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.hilla.engine.fixtures.TestBrowserCallable;
import com.vaadin.hilla.engine.fixtures.TestConfiguration;
import com.vaadin.hilla.engine.fixtures.TestEndpoint;
import com.vaadin.hilla.engine.fixtures.TestMainClass;
import com.vaadin.hilla.engine.fixtures.annotations.BrowserCallable;
import com.vaadin.hilla.engine.fixtures.annotations.Endpoint;

public class AotBrowserCallableFinderTest {
    private static final String TEST_BUILD_DIR_NAME = "target";

    @TempDir
    private Path tempDirectory;

    private Path buildDirectory;

    private Path buildClasssesDirectory;

    private EngineAutoConfiguration testConfiguration;

    @BeforeEach
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("test");
        buildDirectory = tempDirectory.resolve(TEST_BUILD_DIR_NAME);
        buildClasssesDirectory = buildDirectory.resolve("classes");
        Files.createDirectories(buildClasssesDirectory);
        testConfiguration = new EngineAutoConfiguration.Builder()
                .browserCallableFinder(AotBrowserCallableFinder::find)
                .baseDir(tempDirectory).buildDir(TEST_BUILD_DIR_NAME)
                .groupId("com.vaadin.hilla.test").artifactId("test-application")
                .build();
    }

    @AfterEach
    public void tearDown() throws IOException {
        FrontendUtils.deleteDirectory(tempDirectory.toFile());
    }

    @Test
    public void find_Default_ThrowsNoMainClass() throws IOException {
        var exception = assertThrows(BrowserCallableFinderException.class,
                () -> {
                    AotBrowserCallableFinder.find(testConfiguration);
                });
        assertTrue(exception.getMessage().contains("no main class"));
        try (var buildFiles = Files
                .list(tempDirectory.resolve(TEST_BUILD_DIR_NAME))) {
            assertTrue(buildFiles.findAny().isEmpty());
        }
    }

    @Test
    public void find_WithMainClass_ReturnsEmptyList()
            throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(
                testConfiguration).mainClass(TestMainClass.class.getName())
                .build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertTrue(list.isEmpty());
    }

    @Test
    public void find_WithMainClassAndEndpoint_FindsEndpoint()
            throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(
                testConfiguration).endpointAnnotations(Endpoint.class)
                .mainClass(TestMainClass.class.getName()).build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertEquals(list, List.of(TestEndpoint.class));
    }

    @Test
    public void find_WithMainClassAndBrowserCallable_FindsBrowserCallable()
            throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(
                testConfiguration).endpointAnnotations(BrowserCallable.class)
                .mainClass(TestMainClass.class.getName()).build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertEquals(list, List.of(TestBrowserCallable.class));
    }

    @Test
    public void find_WithSourceClasses_FindsEndpoints()
            throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(
                testConfiguration)
                .endpointAnnotations(Endpoint.class, BrowserCallable.class)
                .sourceClasses(List.of(TestConfiguration.class.getName()))
                .build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertEquals(list,
                List.of(TestBrowserCallable.class, TestEndpoint.class));
    }
}
