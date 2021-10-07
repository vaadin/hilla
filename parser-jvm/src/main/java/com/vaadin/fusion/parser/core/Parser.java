package com.vaadin.fusion.parser.core;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class Parser {
    private final Set<String> pluginClassNames = new LinkedHashSet<>();
    private final SharedStorage storage = new SharedStorage();
    private final ParserConfig config;
    private final String classPath;

    public Parser(ParserConfig config) {
        this.config = config;
        this.classPath = config.getClassPath()
                .orElseThrow(() -> new NullPointerException(
                        "Fusion Parser: Classpath is not provided."));
        this.pluginClassNames.addAll(this.config.getPlugins().getUse());
    }

    public void execute() {
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

    private class EntitiesCollector {
        private final RelativeClassList endpoints;
        private final RelativeClassList entities;

        EntitiesCollector(ScanResult result) {
            endpoints = result
                    .getClassesWithAnnotation(config.getApplication().getEndpointAnnotation())
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
