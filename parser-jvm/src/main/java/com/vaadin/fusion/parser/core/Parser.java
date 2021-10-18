package com.vaadin.fusion.parser.core;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class Parser {
    private final ParserConfig config;
    private final SharedStorage storage;

    public Parser(@Nonnull ParserConfig config) {
        this.config = Objects.requireNonNull(config);
        storage = new SharedStorage(config);
    }

    public void execute() {
        PluginManager pluginManager = new PluginManager(config);

        ScanResult result = new ClassGraph().enableAllInfo()
                .overrideClasspath(config.getClassPath())
                .scan();

        EntitiesCollector collector = new EntitiesCollector(result);

        pluginManager.execute(collector.getEndpoints(), collector.getEntities(),
                storage);
    }

    @Nonnull
    public SharedStorage getStorage() {
        return storage;
    }

    private class EntitiesCollector {
        private final List<RelativeClassInfo> endpoints;
        private final List<RelativeClassInfo> entities;

        EntitiesCollector(ScanResult result) {
            endpoints = result
                    .getClassesWithAnnotation(
                            config.getApplication().getEndpointAnnotation())
                    .stream().map(RelativeClassInfo::new)
                    .collect(Collectors.toList());

            entities = endpoints.stream()
                    .flatMap(RelativeClassInfo::getDependencies)
                    .collect(Collectors.toList());

            for (int i = 0; i < entities.size(); i++) {
                RelativeClassInfo entity = entities.get(i);

                entity.getDependencies().filter(dependency -> entities.stream()
                        .noneMatch(d -> Objects.equals(d.get().getName(),
                                dependency.get().getName())))
                        .forEach(entities::add);
            }
        }

        List<RelativeClassInfo> getEndpoints() {
            return endpoints;
        }

        List<RelativeClassInfo> getEntities() {
            return entities;
        }
    }
}
