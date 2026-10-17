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

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.generator.model.EndpointModelPlugin;
import com.vaadin.hilla.generator.model.Generation;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.PluginManager;

public final class ParserProcessor {
    private static final Logger logger = LoggerFactory
            .getLogger(ParserProcessor.class);
    private final Set<Path> classPath;
    private final ParserConfiguration.PluginsProcessor pluginsProcessor = new ParserConfiguration.PluginsProcessor();

    /**
     * Builds what the TypeScript of a run is written from while the parser
     * walks the classes. It only collects: every other plugin sees the walk as
     * it would without it.
     */
    private final EndpointModelPlugin modelPlugin = new EndpointModelPlugin();
    private List<Class<? extends Annotation>> endpointAnnotations = List.of();
    private List<Class<? extends Annotation>> endpointExposedAnnotations = List
            .of();

    public ParserProcessor(EngineAutoConfiguration conf) {
        this.classPath = conf.getClasspath();
        this.endpointAnnotations = conf.getEndpointAnnotations();
        this.endpointExposedAnnotations = conf.getEndpointExposedAnnotations();
        applyConfiguration(conf.getParser());
    }

    /**
     * Runs the parser over the browser callable classes and returns everything
     * the TypeScript of them is written from.
     *
     * @param endpoints
     *            the browser callable classes
     */
    public Generation parse(List<Class<?>> endpoints) throws ParserException {
        var parser = new Parser()
                .classPath(classPath.stream().map(Path::toString)
                        .collect(Collectors.toSet()))
                .endpointAnnotations(endpointAnnotations)
                .endpointExposedAnnotations(endpointExposedAnnotations);

        preparePlugins(parser);

        logger.debug("Starting JVM Parser");

        parser.execute(endpoints);

        return modelPlugin.getGeneration();
    }

    private void applyConfiguration(ParserConfiguration parserConfiguration) {
        if (parserConfiguration == null) {
            return;
        }

        applyEndpointAnnotations(parserConfiguration.getEndpointAnnotations());
        applyEndpointExposedAnnotations(
                parserConfiguration.getEndpointExposedAnnotations());
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

    private void applyPlugins(ParserConfiguration.@NonNull Plugins plugins) {
        this.pluginsProcessor.setConfig(plugins);
    }

    private void preparePlugins(Parser parser) {
        var configuredPlugins = pluginsProcessor.process().stream()
                .map((plugin) -> PluginManager.load(plugin.getName(),
                        plugin.getConfiguration()));

        // The plugin building the model belongs first: a composite plugin
        // exits its plugins in the opposite order, so the first one is the
        // last to see a node, once the others have said everything about it
        var loadedPlugins = Stream
                .concat(Stream.of(modelPlugin), configuredPlugins).toList();

        parser.plugins(loadedPlugins);
    }
}
