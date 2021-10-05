package com.vaadin.fusion.parser;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class Parser {
    private final Set<String> pluginClassNames = new LinkedHashSet<>();
    private final SharedStorage storage = new SharedStorage();
    private String classPath;
    private String endpointAnnotationName;

    Parser() {
    }

    public Parser classPath(String value) {
        classPath = value;

        return this;
    }

    public Parser endpointAnnotationName(String value) {
        endpointAnnotationName = value;

        return this;
    }

    public void execute() {
        Objects.requireNonNull(classPath,
                "Fusion Parser: Classpath is not provided.");
        Objects.requireNonNull(pluginClassNames,
                "Fusion Parser: Plugins are not provided.");
        Objects.requireNonNull(endpointAnnotationName,
                "Fusion Parser: Endpoint annotation name is not provided.");

        PluginManager pluginManager = new PluginManager(pluginClassNames);

        ScanResult result = new ClassGraph().enableAllInfo()
                .overrideClasspath(classPath).scan();

        EntitiesCollector collector = new EntitiesCollector(result);

        pluginManager.execute(collector.getEndpoints(), collector.getEntities(),
                storage);
    }

    public SharedStorage getStorage() {
        return storage;
    }

    public Parser pluginClassNames(Set<String> value) {
        pluginClassNames.addAll(value);
        return this;
    }

    public Parser pluginClassNames(List<String> value) {
        pluginClassNames.addAll(value);
        return this;
    }

    public Parser pluginClassNames(String... value) {
        pluginClassNames.addAll(Arrays.asList(value));
        return this;
    }

    private class EntitiesCollector {
        private final RelativeClassList endpoints;
        private final RelativeClassList entities;

        EntitiesCollector(ScanResult result) {
            endpoints = result.getClassesWithAnnotation(endpointAnnotationName)
                    .stream().map(RelativeClassInfo::new)
                    .collect(Collectors.toCollection(RelativeClassList::new));

            entities = endpoints.stream()
                    .flatMap(element -> element.getDependencies().stream())
                    .collect(Collectors.toCollection(RelativeClassList::new));

            for (int i = 0; i < entities.size(); i++) {
                RelativeClassInfo entity = entities.get(i);

                entity.getDependencies().stream().filter(dependency -> entities
                        .stream()
                        .noneMatch(d -> Objects.equals(d.get().getName(),
                                dependency.get().getName())))
                        .forEach(entities::add);
            }
        }

        RelativeClassList getEndpoints() {
            return endpoints;
        }

        RelativeClassList getEntities() {
            return entities;
        }
    }
}
