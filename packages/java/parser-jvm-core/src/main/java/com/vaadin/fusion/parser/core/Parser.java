package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;

public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final ParserConfig config;
    private final SharedStorage storage;

    public Parser(@Nonnull ParserConfig config) {
        this.config = Objects.requireNonNull(config);
        storage = new SharedStorage(config);
    }

    private static void checkIfJavaCompilerParametersFlagIsEnabled(
            List<RelativeClassInfo> endpoints) {
        endpoints.stream().flatMap(endpoint -> endpoint.getMethods().stream())
                .flatMap(RelativeMethodInfo::getParametersStream).findFirst()
                .ifPresent((parameter) -> {
                    if (parameter.get().getName() == null) {
                        throw new ParserException(
                                "Fusion Parser requires running java compiler with -parameters flag enabled");
                    }
                });
    }

    public OpenAPI execute() {
        logger.debug("Executing JVM Parser");
        var pluginManager = new PluginManager(config);

        logger.debug("Scanning JVM classpath: " + config.getClassPath());
        var result = new ClassGraph().enableAllInfo()
                .overrideClasspath(config.getClassPath()).scan();

        var collector = new EntitiesCollector(result, logger);

        logger.debug("Checking if the compiler is run with -parameters option enabled");
        checkIfJavaCompilerParametersFlagIsEnabled(collector.getEndpoints());

        logger.debug("Executing parser plugins");
        pluginManager.execute(collector.getEndpoints(), collector.getEntities(),
                storage);

        logger.debug("Parsing process successfully finished");
        return storage.getOpenAPI();
    }

    @Nonnull
    public SharedStorage getStorage() {
        return storage;
    }

    private class EntitiesCollector {
        private final List<RelativeClassInfo> endpoints;
        private final List<RelativeClassInfo> entities;

        public EntitiesCollector(ScanResult result, Logger logger) {
            logger.debug("Collecting project endpoints");
            endpoints = result
                    .getClassesWithAnnotation(
                            config.getEndpointAnnotationName())
                    .stream().map(RelativeClassInfo::new)
                    .collect(Collectors.toList());

            logger.debug("Collecting project data entities");
            entities = endpoints.stream().flatMap(
                    cls -> cls.getInheritanceChain().getDependenciesStream())
                    .distinct().collect(Collectors.toList());

            logger.debug("Collecting entities dependencies");
            // ATTENTION: This loop mutates the collection during processing!
            // It is necessary to collect all endpoint dependencies +
            // dependencies of dependencies.
            // Be careful changing it: the endless loop could happen.
            for (var i = 0; i < entities.size(); i++) {
                var entity = entities.get(i);

                entity.getDependenciesStream()
                        .filter(e -> !entities.contains(e))
                        .forEach(entities::add);
            }
        }

        public List<RelativeClassInfo> getEndpoints() {
            return endpoints;
        }

        public List<RelativeClassInfo> getEntities() {
            return entities;
        }
    }
}
