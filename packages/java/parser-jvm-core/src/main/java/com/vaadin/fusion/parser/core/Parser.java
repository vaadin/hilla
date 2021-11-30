package com.vaadin.fusion.parser.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;

public final class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final ParserConfig config;
    private final SharedStorage storage;

    public Parser(@Nonnull ParserConfig config) {
        this.config = Objects.requireNonNull(config);
        storage = new SharedStorage(config);
    }

    private static void checkIfJavaCompilerParametersFlagIsEnabled(
            Collection<RelativeClassInfo> endpoints) {
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

        var classPathElements = config.getClassPathElements();
        logger.debug("Scanning JVM classpath: "
                + String.join(";", classPathElements));
        var result = new ClassGraph().enableAllInfo()
                .overrideClasspath(classPathElements).scan();

        var collector = new EntitiesCollector(result, logger);

        logger.debug(
                "Checking if the compiler is run with -parameters option enabled");
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
        private final Set<RelativeClassInfo> endpoints;
        private final Set<RelativeClassInfo> entities;

        public EntitiesCollector(ScanResult result, Logger logger) {
            var endpointAnnotationName = config.getEndpointAnnotationName();

            logger.debug(
                    "Collecting project endpoints with the endpoint annotation: "
                            + endpointAnnotationName);

            endpoints = result.getClassesWithAnnotation(endpointAnnotationName)
                    .stream().map(RelativeClassInfo::new)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            logger.debug("Collected project endpoints: " + endpoints.stream()
                    .map(RelativeClassInfo::get).map(ClassInfo::getName)
                    .collect(Collectors.joining(", ")));

            entities = endpoints.stream().flatMap(
                    cls -> cls.getInheritanceChain().getDependenciesStream())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // ATTENTION: This loop mutates the collection during processing!
            // It is necessary to collect all endpoint dependencies +
            // dependencies of dependencies.
            // Be careful changing it: the endless loop could happen.
            for (var entity : entities) {
                entity.getDependenciesStream().forEach(entities::add);
            }

            logger.debug("Collected project data entities: " + entities.stream()
                    .map(RelativeClassInfo::get).map(ClassInfo::getName)
                    .collect(Collectors.joining(", ")));
        }

        public Collection<RelativeClassInfo> getEndpoints() {
            return endpoints;
        }

        public Collection<RelativeClassInfo> getEntities() {
            return entities;
        }
    }
}
