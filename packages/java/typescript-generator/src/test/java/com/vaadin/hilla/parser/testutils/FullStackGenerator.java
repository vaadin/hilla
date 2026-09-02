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
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.hilla.EndpointSubscription;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.subtypes.SubTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileCheckerPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

/**
 * Runs the complete endpoint code generation pipeline for a set of browser
 * callable classes and returns the generated TypeScript files.
 *
 * <p>
 * The generator is configured exactly like a real application: the same parser
 * plugins in the same order, and the same TypeScript generator plugins. Tests
 * only need to say which classes to generate from, so that they describe the
 * externally visible contract: given these Java classes, these TypeScript files
 * with this content are produced in these locations.
 */
public final class FullStackGenerator {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FullStackGenerator.class);

    /**
     * The name of the Kotlin nullability plugin, which is only loaded when both
     * the plugin and {@code kotlin-reflect} are available, following the same
     * rule as {@code ParserConfiguration}.
     */
    private static final String KOTLIN_NULLABILITY_PLUGIN = "com.vaadin.hilla.parser.plugins.nonnull.kotlin.KotlinNullabilityPlugin";

    /**
     * The TypeScript generator plugins, in the order used in production.
     */
    private static final List<String> GENERATOR_PLUGINS = List.of(
            "@vaadin/hilla-generator-plugin-transfertypes",
            "@vaadin/hilla-generator-plugin-backbone",
            "@vaadin/hilla-generator-plugin-client",
            "@vaadin/hilla-generator-plugin-model",
            "@vaadin/hilla-generator-plugin-barrel",
            "@vaadin/hilla-generator-plugin-push",
            "@vaadin/hilla-generator-plugin-signals",
            "@vaadin/hilla-generator-plugin-subtypes");

    private final ResourceLoader resourceLoader;
    private final Path targetDir;
    private final List<Class<?>> endpointClasses;
    private final List<Plugin> plugins = defaultParserPlugins();
    private final List<Class<?>> extraClasspath = new ArrayList<>(
            List.of(Flux.class, EndpointSubscription.class));
    private List<Class<? extends Annotation>> endpointAnnotations = List
            .of(Endpoint.class);
    private List<Class<? extends Annotation>> endpointExposedAnnotations = List
            .of(EndpointExposed.class);
    private boolean clientFileIncluded;

    FullStackGenerator(Class<?> testClass, Class<?>... endpointClasses) {
        this.resourceLoader = new ResourceLoader(testClass);
        try {
            this.targetDir = resourceLoader.findTargetDirPath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to find the target folder",
                    e);
        }
        this.endpointClasses = List.of(endpointClasses);
    }

    /**
     * Uses the given plugin instead of the default instance of the same plugin
     * class, keeping the position in the plugin order. Plugins which are not
     * part of the default set are appended.
     *
     * @param plugin
     *            a configured parser plugin
     */
    public FullStackGenerator withPlugin(Plugin plugin) {
        var index = plugins.stream().map(Object::getClass).toList()
                .indexOf(plugin.getClass());

        if (index >= 0) {
            plugins.set(index, plugin);
        } else {
            plugins.add(plugin);
        }

        return this;
    }

    /**
     * Uses the given annotations to detect browser callable classes instead of
     * the default test {@code @Endpoint} annotation.
     */
    @SafeVarargs
    public final FullStackGenerator withEndpointAnnotations(
            Class<? extends Annotation>... annotations) {
        this.endpointAnnotations = List.of(annotations);
        return this;
    }

    /**
     * Uses the given annotations to detect exposed superclasses instead of the
     * default test {@code @EndpointExposed} annotation.
     */
    @SafeVarargs
    public final FullStackGenerator withEndpointExposedAnnotations(
            Class<? extends Annotation>... annotations) {
        this.endpointExposedAnnotations = List.of(annotations);
        return this;
    }

    /**
     * Adds the location of the given classes to the classpath scanned by the
     * parser.
     */
    public FullStackGenerator withClasspathOf(Class<?>... classes) {
        extraClasspath.addAll(List.of(classes));
        return this;
    }

    /**
     * Includes the generated client file in the comparison. It is the same for
     * every endpoint, so it is left out unless a test is about it.
     */
    public FullStackGenerator withClientFile() {
        this.clientFileIncluded = true;
        return this;
    }

    boolean isClientFileIncluded() {
        return clientFileIncluded;
    }

    /**
     * Runs the generator and returns the generated files, keyed by their path
     * relative to the output folder.
     */
    public Map<String, String> generate() {
        return generateTypeScript(parse());
    }

    /**
     * Runs only the Java part of the pipeline. Available for the few tests
     * which assert on things that are not visible in the generated TypeScript.
     */
    public OpenAPI parse() {
        try {
            var classPath = ResourceLoader.getClasspath(extraClasspath.stream()
                    .map(ResourceLoader::new).collect(Collectors.toList()));
            var parser = new Parser()
                    .classPath(classPath.split(File.pathSeparator))
                    .endpointAnnotations(endpointAnnotations)
                    .endpointExposedAnnotations(endpointExposedAnnotations);
            plugins.forEach(parser::addPlugin);

            return parser.execute(endpointClasses);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to build the classpath", e);
        }
    }

    private static List<Plugin> defaultParserPlugins() {
        // Same plugins in the same order as ParserConfiguration uses for a
        // real application.
        var plugins = new ArrayList<Plugin>(List.of(new BackbonePlugin(),
                new MultipartFileCheckerPlugin(), new TransferTypesPlugin()));

        try {
            var pluginClass = Class.forName(KOTLIN_NULLABILITY_PLUGIN);

            // Check that a class from kotlin-reflect is available:
            Class.forName("kotlin.reflect.KClass");

            plugins.add((Plugin) pluginClass.getDeclaredConstructor()
                    .newInstance());
        } catch (Throwable e) {
            LOGGER.debug("Kotlin nullability plugin is not going to be loaded",
                    e);
        }

        plugins.addAll(List.of(new NonnullPlugin(), new SubTypesPlugin(),
                new ModelPlugin()));

        return plugins;
    }

    private Map<String, String> generateTypeScript(OpenAPI openAPI) {
        Path outputDir = null;
        Path openAPIFile = null;

        try {
            var mapper = Json.mapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            outputDir = Files.createTempDirectory("hilla-generator-output");
            openAPIFile = Files.createTempFile("openapi", ".json");
            Files.writeString(openAPIFile, mapper.writeValueAsString(openAPI));

            var command = new ArrayList<>(
                    List.of("node", resolveGeneratorCli().toString(),
                            openAPIFile.toAbsolutePath().toString(), "-o",
                            outputDir.toAbsolutePath().toString()));
            GENERATOR_PLUGINS.forEach(plugin -> {
                command.add("-p");
                command.add(plugin);
            });

            LOGGER.debug("Executing the TypeScript generator: {}",
                    String.join(" ", command));
            FrontendUtils.executeCommand(command,
                    pb -> pb.directory(targetDir.toFile()));

            return readGeneratedFiles(outputDir);
        } catch (IOException | FrontendUtils.CommandExecutionException e) {
            throw new IllegalStateException(
                    "Unable to generate TypeScript files", e);
        } finally {
            deleteRecursively(openAPIFile);
            deleteRecursively(outputDir);
        }
    }

    private static Map<String, String> readGeneratedFiles(Path outputDir)
            throws IOException {
        try (var files = Files.walk(outputDir)) {
            // A sorted map keeps assertion failures readable
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".ts")).sorted()
                    .collect(Collectors.toMap(
                            path -> outputDir.relativize(path).toString()
                                    .replace(File.separatorChar, '/'),
                            path -> readString(path), (a, b) -> a,
                            LinkedHashMap::new));
        }
    }

    private static String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to read the generated file " + path, e);
        }
    }

    /**
     * Resolves the generator CLI the same way Flow resolves an npm package
     * executable.
     */
    private Path resolveGeneratorCli()
            throws FrontendUtils.CommandExecutionException {
        var script = """
                var path = require('path');
                var jsonPath = require.resolve('@vaadin/hilla-generator-cli/package.json');
                var json = require(jsonPath);
                console.log(path.resolve(path.dirname(jsonPath), json.bin['tsgen']));
                """;

        return Path.of(
                FrontendUtils.executeCommand(List.of("node", "--eval", script),
                        pb -> pb.directory(targetDir.toFile())).trim());
    }

    private static void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }

        try (var paths = Files.walk(path)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(entry -> {
                try {
                    Files.deleteIfExists(entry);
                } catch (IOException e) {
                    LOGGER.warn("Unable to delete {}", entry, e);
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Unable to clean up {}", path, e);
        }
    }

    /**
     * Returns the module folder, which is the parent of the build folder.
     */
    Path getModuleDir() {
        return targetDir.getParent();
    }
}
