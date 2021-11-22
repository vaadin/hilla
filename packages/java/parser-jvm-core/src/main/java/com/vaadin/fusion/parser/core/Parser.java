package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;

public class Parser {
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
        var pluginManager = new PluginManager(config);

        var result = new ClassGraph().enableAllInfo()
                .overrideClasspath(config.getClassPath()).scan();

        var collector = new EntitiesCollector(result);

        checkIfJavaCompilerParametersFlagIsEnabled(collector.getEndpoints());

        pluginManager.execute(collector.getEndpoints(), collector.getEntities(),
                storage);

        return storage.getOpenAPI();
    }

    @Nonnull
    public SharedStorage getStorage() {
        return storage;
    }

    private class EntitiesCollector {
        private final List<RelativeClassInfo> endpoints;
        private final List<RelativeClassInfo> entities;

        public EntitiesCollector(ScanResult result) {
            endpoints = result
                    .getClassesWithAnnotation(
                            config.getEndpointAnnotationName())
                    .stream().map(RelativeClassInfo::new)
                    .collect(Collectors.toList());

            entities = endpoints.stream().flatMap(
                    cls -> cls.getInheritanceChain().getDependenciesStream())
                    .distinct().collect(Collectors.toList());

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
