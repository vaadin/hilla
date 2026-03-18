package com.vaadin.hilla.engine;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.hilla.engine.fixtures.TestBrowserCallable;
import com.vaadin.hilla.engine.fixtures.TestConfiguration;
import com.vaadin.hilla.engine.fixtures.TestEndpoint;
import com.vaadin.hilla.engine.fixtures.TestMainClass;
import com.vaadin.hilla.engine.fixtures.annotations.BrowserCallable;
import com.vaadin.hilla.engine.fixtures.annotations.Endpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                .baseDir(tempDirectory)
                .buildDir(TEST_BUILD_DIR_NAME)
                .groupId("com.vaadin.hilla.test")
                .artifactId("test-application")
                .build();
    }

    @AfterEach
    public void tearDown() throws IOException {
        FrontendUtils.deleteDirectory(tempDirectory.toFile());
    }

    @Test
    public void find_Default_ThrowsNoMainClass() throws IOException {
        var exception = assertThrows(BrowserCallableFinderException.class, () -> {
            AotBrowserCallableFinder.find(testConfiguration);
        });
        assertTrue(exception.getMessage().contains("no main class"));
        try (var buildFiles = Files.list(tempDirectory.resolve(TEST_BUILD_DIR_NAME))) {
            assertTrue(buildFiles.findAny().isEmpty());
        }
    }

    @Test
    public void find_WithMainClass_ReturnsEmptyList() throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(testConfiguration)
                .mainClass(TestMainClass.class.getName()).build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertTrue(list.isEmpty());
    }

    @Test
    public void find_WithMainClassAndEndpoint_FindsEndpoint() throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(testConfiguration)
                .endpointAnnotations(Endpoint.class)
                .mainClass(TestMainClass.class.getName())
                .build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertEquals(list, List.of(TestEndpoint.class));
    }

    @Test
    public void find_WithMainClassAndBrowserCallable_FindsBrowserCallable() throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(testConfiguration)
                .endpointAnnotations(BrowserCallable.class)
                .mainClass(TestMainClass.class.getName())
                .build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertEquals(list, List.of(TestBrowserCallable.class));
    }

    @Test
    public void find_WithSourceClasses_FindsEndpoints() throws BrowserCallableFinderException {
        var configuration = new EngineAutoConfiguration.Builder(testConfiguration)
                .endpointAnnotations(Endpoint.class, BrowserCallable.class)
                .sourceClasses(List.of(TestConfiguration.class.getName()))
                .build();
        var list = AotBrowserCallableFinder.find(configuration);
        assertEquals(list, List.of(TestBrowserCallable.class, TestEndpoint.class));
    }
}