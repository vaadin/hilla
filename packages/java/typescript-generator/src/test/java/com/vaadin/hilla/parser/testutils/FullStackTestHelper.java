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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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

/**
 * Helper for running full-stack tests that verify Java → TypeScript code
 * generation.
 */
public class FullStackTestHelper {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FullStackTestHelper.class);

    private final ResourceLoader resourceLoader;
    private final Path targetDir;
    private final ObjectMapper objectMapper;

    public FullStackTestHelper(Class<?> testClass) {
        try {
            this.resourceLoader = new ResourceLoader(testClass);
            this.targetDir = resourceLoader.findTargetDirPath();

            // Use Jackson 2 ObjectMapper for OpenAPI serialization
            this.objectMapper = Json.mapper();
            this.objectMapper
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(
                    "Failed to initialize FullStackTestHelper", e);
        }
    }

    /**
     * Execute the full stack: Java → OpenAPI → TypeScript
     *
     * @param openAPI
     *            The OpenAPI specification to process
     * @return The generated TypeScript files
     * @throws FullStackExecutionException
     *             if generation fails
     */
    public GeneratedFiles executeFullStack(OpenAPI openAPI)
            throws FullStackExecutionException {
        try {
            // Serialize OpenAPI to JSON
            String openAPIJson = objectMapper.writeValueAsString(openAPI);

            // Find the run-generator.mjs script
            Path scriptPath = findGeneratorScript();

            // Execute the generator via Node.js
            NodeRunner nodeRunner = new NodeRunner(targetDir.toFile());
            NodeRunner.NodeResult result = nodeRunner.execute(
                    scriptPath.toFile().getAbsolutePath(), openAPIJson);

            // Parse the output JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> resultJson = objectMapper
                    .readValue(result.getStdout(), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, String>> filesJson = (List<Map<String, String>>) resultJson
                    .get("files");

            if (filesJson == null) {
                throw new FullStackExecutionException(
                        "Generator output missing 'files' property");
            }

            // Convert to GeneratedFiles
            Map<String, String> files = new HashMap<>();
            for (Map<String, String> fileJson : filesJson) {
                String name = fileJson.get("name");
                String content = fileJson.get("content");
                if (name != null && content != null) {
                    files.put(name, content);
                }
            }

            LOGGER.info("Generated {} TypeScript files", files.size());
            return new GeneratedFiles(files);

        } catch (JsonProcessingException e) {
            throw new FullStackExecutionException(
                    "Failed to serialize OpenAPI to JSON", e);
        } catch (NodeRunner.NodeExecutionException e) {
            throw new FullStackExecutionException(
                    "Failed to execute TypeScript generator", e);
        } catch (IOException e) {
            throw new FullStackExecutionException(
                    "Failed to parse generator output", e);
        }
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
    public void assertTypescriptMatches(GeneratedFiles generated,
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
    public Path getSnapshotsDir() throws URISyntaxException, IOException {
        return resourceLoader.find("snapshots").toPath();
    }

    /**
     * Get the target directory for the test class.
     */
    public Path getTargetDir() {
        return targetDir;
    }

    private Path findGeneratorScript() throws FullStackExecutionException {
        try {
            // Script is in test/resources directory root
            // Use absolute path from resources root (leading slash)
            var resource = getClass().getResource("/run-generator.mjs");
            if (resource == null) {
                throw new FullStackExecutionException(
                        "Generator script not found in test resources: /run-generator.mjs");
            }
            Path scriptPath = Path.of(resource.toURI());
            if (!Files.exists(scriptPath)) {
                throw new FullStackExecutionException(
                        "Generator script not found: " + scriptPath);
            }
            return scriptPath;
        } catch (URISyntaxException e) {
            throw new FullStackExecutionException(
                    "Failed to find generator script", e);
        }
    }

    /**
     * Container for generated TypeScript files.
     */
    public static class GeneratedFiles {
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
    public static class FullStackExecutionException extends Exception {
        public FullStackExecutionException(String message) {
            super(message);
        }

        public FullStackExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
