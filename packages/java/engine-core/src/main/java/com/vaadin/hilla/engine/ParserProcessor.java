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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.generator.model.EndpointModelPlugin;
import com.vaadin.hilla.generator.model.Generation;
import com.vaadin.hilla.parser.core.OpenAPIFileType;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.PluginManager;
import com.vaadin.hilla.parser.utils.JsonPrinter;

public final class ParserProcessor {
    private static final Logger logger = LoggerFactory
            .getLogger(ParserProcessor.class);
    private final Path baseDir;
    private final Set<Path> classPath;
    private final Path openAPIFile;
    private final ParserConfiguration.PluginsProcessor pluginsProcessor = new ParserConfiguration.PluginsProcessor();

    /**
     * Builds what the TypeScript of a run is written from while the parser
     * walks the classes, where that is what it is written from. It only
     * collects: the OpenAPI definition is what it would be without it.
     */
    private final EndpointModelPlugin modelPlugin = new EndpointModelPlugin();
    private List<Class<? extends Annotation>> endpointAnnotations = List.of();
    private List<Class<? extends Annotation>> endpointExposedAnnotations = List
            .of();
    private String openAPIBasePath;

    public ParserProcessor(EngineAutoConfiguration conf) {
        this.baseDir = conf.getBaseDir();
        this.openAPIFile = conf.getOpenAPIFile();
        this.classPath = conf.getClasspath();
        this.endpointAnnotations = conf.getEndpointAnnotations();
        this.endpointExposedAnnotations = conf.getEndpointExposedAnnotations();
        applyConfiguration(conf.getParser());
    }

    private String createOpenAPI(List<Class<?>> endpoints) throws IOException {
        var parser = new Parser()
                .classPath(classPath.stream().map(Path::toString)
                        .collect(Collectors.toSet()))
                .endpointAnnotations(endpointAnnotations)
                .endpointExposedAnnotations(endpointExposedAnnotations);

        preparePlugins(parser);
        prepareOpenAPIBase(parser);

        logger.debug("Starting JVM Parser");

        var openAPI = parser.execute(endpoints);

        return new JsonPrinter().pretty().writeAsString(openAPI);
    }

    /**
     * Everything the last run of the parser found, which is what the TypeScript
     * of the endpoints is written from. There is nothing to find unless that is
     * where it is written, since building it is work for nothing then.
     *
     * @see GeneratorProcessor#writesTypeScriptInJava()
     */
    public Generation getGeneration() {
        return modelPlugin.getGeneration();
    }

    public void process(List<Class<?>> endpoints) throws ParserException {
        String openAPIString;

        try {
            Files.createDirectories(openAPIFile.getParent());
            openAPIString = createOpenAPI(endpoints);
        } catch (IOException e) {
            throw new ParserException("Unable to prepare OpenAPI definition",
                    e);
        }

        // Only save the file if it has changed
        Optional.of(openAPIFile).filter(Files::isRegularFile)
                .map(this::readFromFile).filter(openAPIString::equals)
                .ifPresentOrElse(s -> {
                    logger.debug("OpenAPI definition has not changed");
                }, () -> {
                    try {
                        Files.write(openAPIFile, openAPIString.getBytes());
                        logger.debug("OpenAPI definition file saved");
                    } catch (IOException e) {
                        throw new ParserException("Unable to save OpenAPI file",
                                e);
                    }
                });
    }

    private void applyConfiguration(ParserConfiguration parserConfiguration) {
        if (parserConfiguration == null) {
            return;
        }

        applyEndpointAnnotations(parserConfiguration.getEndpointAnnotations());
        applyEndpointExposedAnnotations(
                parserConfiguration.getEndpointExposedAnnotations());
        parserConfiguration.getOpenAPIBasePath()
                .ifPresent(this::applyOpenAPIBase);
        parserConfiguration.getPlugins().ifPresent(this::applyPlugins);
    }

    private void applyEndpointAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointAnnotations) {
        this.endpointAnnotations = Objects.requireNonNull(endpointAnnotations);
    }

    private void applyEndpointExposedAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointExposedAnnotations) {
        this.endpointExposedAnnotations = Objects
                .requireNonNull(endpointExposedAnnotations);
    }

    private void applyOpenAPIBase(@NonNull String openAPIBasePath) {
        this.openAPIBasePath = openAPIBasePath;
    }

    private void applyPlugins(ParserConfiguration.@NonNull Plugins plugins) {
        this.pluginsProcessor.setConfig(plugins);
    }

    private void prepareOpenAPIBase(Parser parser) {
        if (openAPIBasePath == null) {
            return;
        }

        try {
            var path = baseDir.resolve(openAPIBasePath);
            var fileName = path.getFileName().toString();

            if (!fileName.endsWith("yml") && !fileName.endsWith("yaml")
                    && !fileName.endsWith("json")) {
                throw new IOException("No OpenAPI base file found");
            }

            parser.openAPISource(Files.readString(path),
                    fileName.endsWith("json") ? OpenAPIFileType.JSON
                            : OpenAPIFileType.YAML);
        } catch (IOException e) {
            throw new ParserException("Failed loading OpenAPI spec file", e);
        }
    }

    private void preparePlugins(Parser parser) {
        var configuredPlugins = pluginsProcessor.process().stream()
                .map((plugin) -> PluginManager.load(plugin.getName(),
                        plugin.getConfiguration()));

        // The plugin building the model only runs where the model is what the
        // TypeScript is written from. It belongs first: a composite plugin
        // exits its plugins in the opposite order, so the first one is the
        // last to see a node, once the others have said everything about it
        var loadedPlugins = GeneratorProcessor.writesTypeScriptInJava()
                ? Stream.concat(Stream.of(modelPlugin), configuredPlugins)
                        .toList()
                : configuredPlugins.toList();

        parser.plugins(loadedPlugins);
    }

    // Workaround for IOException in lambda
    private String readFromFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            logger.error("Unable to read file", e);
            return null;
        }
    }
}
