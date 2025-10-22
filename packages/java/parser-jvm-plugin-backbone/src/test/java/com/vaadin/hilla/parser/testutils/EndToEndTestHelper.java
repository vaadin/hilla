package com.vaadin.hilla.parser.testutils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Test helper that runs the complete end-to-end generation pipeline:
 * Java classes → OpenAPI → TypeScript files.
 * <p>
 * This helper executes the full pipeline within a Java test, including
 * invoking the Node.js TypeScript generator, and returns the generated
 * TypeScript files for assertion.
 */
public class EndToEndTestHelper {
    private final List<Class<?>> endpoints = new ArrayList<>();
    private final List<Class<? extends Annotation>> endpointAnnotations = new ArrayList<>();
    private final List<Class<? extends Annotation>> endpointExposedAnnotations = new ArrayList<>();
    private final List<Plugin> additionalPlugins = new ArrayList<>();
    private final Path tempDir;
    private final ResourceLoader resourceLoader;
    private final Class<?> testClass;

    public EndToEndTestHelper(Class<?> testClass) throws IOException {
        this.testClass = testClass;
        this.tempDir = Files.createTempDirectory("hilla-e2e-test-");
        this.resourceLoader = new ResourceLoader(testClass);
    }

    /**
     * Specify endpoint classes to process.
     */
    public EndToEndTestHelper withEndpoints(Class<?>... endpoints) {
        this.endpoints.addAll(Arrays.asList(endpoints));
        return this;
    }

    /**
     * Specify annotations that mark classes as endpoints.
     */
    @SafeVarargs
    public final EndToEndTestHelper withEndpointAnnotations(Class<? extends Annotation>... annotations) {
        this.endpointAnnotations.addAll(Arrays.asList(annotations));
        return this;
    }

    /**
     * Specify annotations that mark endpoint members as exposed.
     */
    @SafeVarargs
    public final EndToEndTestHelper withEndpointExposedAnnotations(Class<? extends Annotation>... annotations) {
        this.endpointExposedAnnotations.addAll(Arrays.asList(annotations));
        return this;
    }

    /**
     * Add additional parser plugins to run after BackbonePlugin.
     */
    public EndToEndTestHelper withPlugins(Plugin... plugins) {
        this.additionalPlugins.addAll(Arrays.asList(plugins));
        return this;
    }

    /**
     * Runs the full generation pipeline and returns generated TypeScript files.
     *
     * @return Map of relative file paths to file contents (e.g., "UserEndpoint.ts" → "import ...")
     * @throws Exception if generation fails
     */
    public Map<String, String> generate() throws Exception {
        // Step 1: Parse Java → OpenAPI
        var openAPI = parseJavaToOpenAPI();

        // Step 2: Write OpenAPI to temp file
        var openAPIFile = tempDir.resolve("openapi.json");
        writeOpenAPI(openAPI, openAPIFile);

        // Step 3: Run TypeScript generator (Node.js) on OpenAPI
        var outputDir = tempDir.resolve("generated");
        runTypeScriptGenerator(openAPIFile, outputDir);

        // Step 4: Read all generated .ts files
        var files = readGeneratedFiles(outputDir);
        return files;
    }

    /**
     * Clean up temporary files.
     */
    public void cleanup() throws IOException {
        if (Files.exists(tempDir)) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                // Ignore cleanup errors
                            }
                        });
            }
        }
    }

    private OpenAPI parseJavaToOpenAPI() throws URISyntaxException {
        var parser = new Parser()
                .classPath(Set.of(resourceLoader.findTargetDirPath().toString()))
                .endpointAnnotations(endpointAnnotations)
                .endpointExposedAnnotations(endpointExposedAnnotations);

        // Add all standard parser plugins
        addParserPlugins(parser);

        return parser.execute(endpoints);
    }

    private void addParserPlugins(Parser parser) {
        // Always add BackbonePlugin first
        parser.addPlugin(new BackbonePlugin());

        // Add any additional plugins specified by the test
        for (var plugin : additionalPlugins) {
            parser.addPlugin(plugin);
        }
    }

    private void writeOpenAPI(OpenAPI openAPI, Path file) throws IOException {
        var mapper = Json.mapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        var json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(openAPI);
        Files.writeString(file, json);
    }

    private void runTypeScriptGenerator(Path openAPIFile, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);

        // Find Node.js command
        var nodeCommand = findNodeCommand();

        // Find @vaadin/hilla-generator-cli package
        var generatorCli = findGeneratorCli();

        // Find generator plugins
        var pluginPaths = findGeneratorPlugins();

        // Build command
        var command = new ArrayList<String>();
        command.add(nodeCommand);
        command.add(generatorCli.toString());
        command.add(openAPIFile.toString());
        command.add("-o");
        command.add(outputDir.toString());

        // Add all plugins
        for (var pluginPath : pluginPaths) {
            command.add("-p");
            command.add(pluginPath.toString());
        }

        // Run Node.js generator
        var processBuilder = new ProcessBuilder(command)
                .redirectErrorStream(true);

        var process = processBuilder.start();

        // Capture output for debugging
        var output = new String(process.getInputStream().readAllBytes());

        var exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "TypeScript generation failed with exit code " + exitCode + ":\n" + output);
        }
    }

    private String findNodeCommand() {
        // Check NODE_HOME environment variable
        var nodeHome = System.getenv("NODE_HOME");
        if (nodeHome != null) {
            var nodePath = Path.of(nodeHome, "bin", "node");
            if (Files.exists(nodePath)) {
                return nodePath.toString();
            }
        }

        // Default to "node" from PATH
        return "node";
    }

    private Path findGeneratorCli() throws IOException, InterruptedException {
        // Try to find via npm root
        var npmRootProcess = new ProcessBuilder("npm", "root")
                .redirectErrorStream(true)
                .start();

        var npmRoot = new String(npmRootProcess.getInputStream().readAllBytes()).trim();
        var exitCode = npmRootProcess.waitFor();

        if (exitCode == 0 && !npmRoot.isEmpty()) {
            var cliPath = Path.of(npmRoot, "@vaadin", "hilla-generator-cli", "bin", "index.js");
            if (Files.exists(cliPath)) {
                return cliPath;
            }
        }

        // Try to find in project's node_modules
        var projectRoot = findProjectRoot();
        if (projectRoot != null) {
            var cliPath = projectRoot.resolve("node_modules/@vaadin/hilla-generator-cli/bin/index.js");
            if (Files.exists(cliPath)) {
                return cliPath;
            }
        }

        throw new RuntimeException(
                "Could not find @vaadin/hilla-generator-cli. " +
                "Make sure npm packages are installed (run 'npm install' in project root).");
    }

    private List<Path> findGeneratorPlugins() throws IOException, InterruptedException {
        var plugins = new ArrayList<Path>();

        // Standard plugin names in the order they should be loaded
        var pluginNames = List.of(
                "backbone",
                "client",
                "model",
                "barrel",
                "push",
                "subtypes"
        );

        // Try to find via npm root
        var npmRootProcess = new ProcessBuilder("npm", "root")
                .redirectErrorStream(true)
                .start();

        var npmRoot = new String(npmRootProcess.getInputStream().readAllBytes()).trim();
        var exitCode = npmRootProcess.waitFor();

        Path nodeModules = null;
        if (exitCode == 0 && !npmRoot.isEmpty()) {
            nodeModules = Path.of(npmRoot);
        } else {
            // Try project root
            var projectRoot = findProjectRoot();
            if (projectRoot != null) {
                nodeModules = projectRoot.resolve("node_modules");
            }
        }

        if (nodeModules == null) {
            throw new RuntimeException("Could not find node_modules directory");
        }

        // Find each plugin
        for (var pluginName : pluginNames) {
            var pluginPath = nodeModules.resolve("@vaadin/hilla-generator-plugin-" + pluginName + "/index.js");
            if (Files.exists(pluginPath)) {
                plugins.add(pluginPath);
            }
        }

        if (plugins.isEmpty()) {
            throw new RuntimeException(
                    "Could not find any generator plugins in " + nodeModules +
                    ". Make sure npm packages are installed (run 'npm install' in project root).");
        }

        return plugins;
    }

    private Path findProjectRoot() {
        // Walk up from test class location to find project root
        try {
            var current = resourceLoader.findTargetPath();
            while (current != null && current.getParent() != null) {
                // Look for package.json at root
                var packageJson = current.resolve("package.json");
                if (Files.exists(packageJson)) {
                    return current;
                }

                // Look for pom.xml at root (multi-module indicator)
                var pomXml = current.resolve("pom.xml");
                if (Files.exists(pomXml)) {
                    var content = Files.readString(pomXml);
                    // Check if this is the root pom (has hilla-project artifact)
                    if (content.contains("<artifactId>hilla-project</artifactId>")) {
                        return current;
                    }
                }

                current = current.getParent();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private Map<String, String> readGeneratedFiles(Path dir) throws IOException {
        var files = new HashMap<String, String>();

        if (!Files.exists(dir)) {
            return files;
        }

        try (var stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".ts"))
                    .forEach(p -> {
                        try {
                            var relativePath = dir.relativize(p).toString();
                            // Normalize path separators to forward slashes
                            relativePath = relativePath.replace('\\', '/');
                            var content = Files.readString(p);
                            files.put(relativePath, content);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        return files;
    }
}
