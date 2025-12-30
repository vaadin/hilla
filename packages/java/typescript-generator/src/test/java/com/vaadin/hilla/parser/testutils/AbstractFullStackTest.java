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
package com.vaadin.hilla.parser.testutils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import com.vaadin.hilla.EndpointSubscription;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.subtypes.SubTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileCheckerPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

/**
 * Abstract base class for full-stack tests that verify Java → TypeScript code
 * generation.
 *
 * <p>
 * This class provides a simplified testing approach where all common
 * configuration is pre-configured:
 * <ul>
 * <li>All plugins (Backbone, TransferTypes, Model, Nonnull, SubTypes,
 * MultipartFileChecker)</li>
 * <li>Extended classpath (includes Flux and EndpointSubscription)</li>
 * <li>Both @Endpoint and @EndpointExposed annotations</li>
 * </ul>
 *
 * <p>
 * Usage example:
 *
 * <pre>
 * public class MyEndpointTest extends AbstractFullStackTest {
 *     &#64;Test
 *     public void should_GenerateCorrectTypeScript() throws Exception {
 *         assertTypescriptMatchesSnapshot(MyEndpoint.class);
 *     }
 * }
 * </pre>
 */
public abstract class AbstractFullStackTest {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractFullStackTest.class);

    private final ResourceLoader resourceLoader;
    private final Path targetDir;
    private final ObjectMapper objectMapper;

    protected AbstractFullStackTest() {
        try {
            this.resourceLoader = new ResourceLoader(getClass());
            this.targetDir = resourceLoader.findTargetDirPath();

            // Use Jackson 2 ObjectMapper for OpenAPI serialization
            this.objectMapper = Json.mapper();
            this.objectMapper
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(
                    "Failed to initialize AbstractFullStackTest", e);
        }
    }

    /**
     * Execute the full Java → TypeScript pipeline and verify the generated
     * TypeScript matches the expected snapshots.
     *
     * @param endpointClasses
     *            The endpoint classes to process
     * @throws Exception
     *             if the test fails
     */
    protected void assertTypescriptMatchesSnapshot(Class<?>... endpointClasses)
            throws Exception {
        // Build extended classpath that includes all required dependencies
        var classpath = ResourceLoader.getClasspath(Arrays.stream(
                new Class<?>[] { Flux.class, EndpointSubscription.class })
                .map(ResourceLoader::new).collect(Collectors.toList()));

        // Parse Java → OpenAPI with all plugins
        var openAPI = new Parser()
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin())
                .addPlugin(new ModelPlugin()).addPlugin(new NonnullPlugin())
                .addPlugin(new SubTypesPlugin())
                .addPlugin(new MultipartFileCheckerPlugin())
                .execute(Arrays.asList(endpointClasses));

        // OpenAPI → TypeScript, then verify against snapshots
        var generated = executeFullStack(openAPI);
        assertTypescriptMatches(generated, getSnapshotsDir());
    }

    // Standard Hilla generator plugins in the correct loading order
    private static final List<String> GENERATOR_PLUGINS = List.of(
            "@vaadin/hilla-generator-plugin-transfertypes",
            "@vaadin/hilla-generator-plugin-backbone",
            "@vaadin/hilla-generator-plugin-client",
            "@vaadin/hilla-generator-plugin-model",
            "@vaadin/hilla-generator-plugin-barrel",
            "@vaadin/hilla-generator-plugin-push",
            "@vaadin/hilla-generator-plugin-signals",
            "@vaadin/hilla-generator-plugin-subtypes");

    /**
     * Execute the full stack: Java → OpenAPI → TypeScript
     *
     * @param openAPI
     *            The OpenAPI specification to process
     * @return The generated TypeScript files
     * @throws FullStackExecutionException
     *             if generation fails
     */
    private GeneratedFiles executeFullStack(OpenAPI openAPI)
            throws FullStackExecutionException {
        Path tempDir = null;
        Path tempFile = null;
        try {
            // Serialize OpenAPI to JSON
            String openAPIJson = objectMapper.writeValueAsString(openAPI);

            // Find the CLI executable using require.resolve pattern
            Path cliPath = resolveCliPath();

            // Create temp output directory and OpenAPI file
            tempDir = Files.createTempDirectory("hilla-test-output");
            tempFile = Files.createTempFile("openapi", ".json");
            Files.writeString(tempFile, openAPIJson);

            // Build CLI command with all plugins
            var command = new java.util.ArrayList<String>();
            command.add("node");
            command.add(cliPath.toString());
            command.add(tempFile.toAbsolutePath().toString());
            command.add("-o");
            command.add(tempDir.toAbsolutePath().toString());
            for (String plugin : GENERATOR_PLUGINS) {
                command.add("-p");
                command.add(plugin);
            }

            LOGGER.debug("Executing CLI: {}", String.join(" ", command));

            // Execute the CLI
            com.vaadin.flow.server.frontend.FrontendUtils.executeCommand(
                    command, pb -> pb.directory(targetDir.toFile()));

            // Read generated files from output directory
            Map<String, String> files = new HashMap<>();
            final Path outputDir = tempDir;
            Files.walk(outputDir).filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".ts")).forEach(path -> {
                        try {
                            String relativePath = outputDir.relativize(path)
                                    .toString();
                            String content = Files.readString(path);
                            files.put(relativePath, content);
                        } catch (IOException e) {
                            throw new RuntimeException(
                                    "Failed to read generated file: " + path,
                                    e);
                        }
                    });

            LOGGER.info("Generated {} TypeScript files", files.size());
            return new GeneratedFiles(files);

        } catch (JsonProcessingException e) {
            throw new FullStackExecutionException(
                    "Failed to serialize OpenAPI to JSON", e);
        } catch (com.vaadin.flow.server.frontend.FrontendUtils.CommandExecutionException e) {
            throw new FullStackExecutionException(
                    "Failed to execute TypeScript generator CLI", e);
        } catch (IOException e) {
            throw new FullStackExecutionException(
                    "Failed during full-stack execution", e);
        } finally {
            // Clean up temp files
            try {
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile);
                }
                if (tempDir != null) {
                    deleteRecursively(tempDir);
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to clean up temp files", e);
            }
        }
    }

    /**
     * Resolve the path to the Hilla generator CLI executable using Node's
     * require.resolve.
     *
     * @see <a href=
     *      "https://github.com/vaadin/flow/blob/main/flow-server/src/main/java/com/vaadin/flow/server/frontend/FrontendTools.java">
     *      Flow's FrontendTools.getNpmPackageExecutable</a>
     */
    private Path resolveCliPath() throws FullStackExecutionException {
        try {
            String script = """
                    var path = require('path');
                    var jsonPath = require.resolve('@vaadin/hilla-generator-cli/package.json');
                    var json = require(jsonPath);
                    console.log(path.resolve(path.dirname(jsonPath), json.bin['tsgen']));
                    """;
            String cliPath = com.vaadin.flow.server.frontend.FrontendUtils
                    .executeCommand(List.of("node", "--eval", script),
                            pb -> pb.directory(targetDir.toFile()))
                    .trim();
            return Path.of(cliPath);
        } catch (com.vaadin.flow.server.frontend.FrontendUtils.CommandExecutionException e) {
            throw new FullStackExecutionException("Failed to resolve CLI path",
                    e);
        }
    }

    /**
     * Recursively delete a directory and its contents.
     */
    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var entries = Files.list(path)) {
                for (Path entry : entries.toList()) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    /**
     * Assert that generated TypeScript matches expected snapshots.
     *
     * @param generated
     *            The generated files
     * @param snapshotsDir
     *            Directory containing expected .ts files
     * @throws AssertionError
     *             if files don't match
     */
    private void assertTypescriptMatches(GeneratedFiles generated,
            Path snapshotsDir) throws IOException {
        if (!Files.exists(snapshotsDir)) {
            throw new AssertionError(
                    "Snapshots directory does not exist: " + snapshotsDir);
        }

        // Read all expected files
        Map<String, String> expected = new HashMap<>();
        Files.walk(snapshotsDir).filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".ts")).forEach(path -> {
                    try {
                        String relativePath = snapshotsDir.relativize(path)
                                .toString();
                        String content = Files.readString(path);
                        expected.put(relativePath, content);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Failed to read snapshot: " + path, e);
                    }
                });

        // Compare generated vs expected
        TypeScriptComparator comparator = new TypeScriptComparator();
        comparator.compare(expected, generated.getFiles());
    }

    /**
     * Get the snapshots directory for the test class.
     */
    private Path getSnapshotsDir() throws URISyntaxException, IOException {
        return resourceLoader.find("snapshots").toPath();
    }

    /**
     * Container for generated TypeScript files.
     */
    private static class GeneratedFiles {
        private final Map<String, String> files;

        public GeneratedFiles(Map<String, String> files) {
            this.files = Collections.unmodifiableMap(files);
        }

        public Map<String, String> getFiles() {
            return files;
        }

        public String getFile(String name) {
            return files.get(name);
        }

        public boolean hasFile(String name) {
            return files.containsKey(name);
        }

        public int getFileCount() {
            return files.size();
        }

        public List<String> getFileNames() {
            return files.keySet().stream().sorted()
                    .collect(Collectors.toList());
        }
    }

    /**
     * Exception thrown when full-stack execution fails.
     */
    private static class FullStackExecutionException extends Exception {
        public FullStackExecutionException(String message) {
            super(message);
        }

        public FullStackExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
