package com.vaadin.fusion.parser.core.dependency;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;
import com.vaadin.fusion.parser.testutils.PluginElementsFilter;

public class DependencyPlugin implements Plugin {
    public static final String ALL_DEPS_STORAGE_KEY = "DependencyPlugin_AllDeps";
    public static final String DEPS_MEMBERS_STORAGE_KEY = "DependencyPlugin_DepsMembers";
    public static final String ENDPOINTS_DIRECT_DEPS_STORAGE_KEY = "DependencyPlugin_EndpointsDirectDeps";

    protected final PluginElementsFilter filter = new PluginElementsFilter(
            "Dependency");

    @Override
    public void execute(@Nonnull List<RelativeClassInfo> endpoints,
            @Nonnull List<RelativeClassInfo> entities, SharedStorage storage) {
        var collector = new DependencyCollector(
                filter.apply(endpoints), filter.apply(entities));

        storage.getPluginStorage().put(ALL_DEPS_STORAGE_KEY,
                collector.collectAllDependencyNames());

        storage.getPluginStorage().put(DEPS_MEMBERS_STORAGE_KEY,
                collector.collectDependencyMemberNames());

        storage.getPluginStorage().put(ENDPOINTS_DIRECT_DEPS_STORAGE_KEY,
                collector.collectEndpointDirectDependencyNames());
    }

    private static class DependencyCollector {
        private final List<RelativeClassInfo> endpoints;
        private final List<RelativeClassInfo> entities;

        DependencyCollector(List<RelativeClassInfo> endpoints,
                List<RelativeClassInfo> entities) {
            this.endpoints = endpoints;
            this.entities = entities;
        }

        List<String> collectAllDependencyNames() {
            return entities.stream()
                    .map(dependency -> dependency.get().getName())
                    .collect(Collectors.toList());
        }

        List<String> collectDependencyMemberNames() {
            return Stream.of(
                    entities.stream().flatMap(RelativeClassInfo::getFieldsStream)
                            .map(field -> field.get().getName()),
                    entities.stream().flatMap(RelativeClassInfo::getMethodsStream)
                            .map(method -> method.get().getName()),
                    entities.stream()
                            .flatMap(RelativeClassInfo::getInnerClassesStream)
                            .map(cls -> cls.get().getName()))
                    .flatMap(Function.identity()).collect(Collectors.toList());
        }

        List<String> collectEndpointDirectDependencyNames() {
            return endpoints.stream()
                    .flatMap(RelativeClassInfo::getDependenciesStream)
                    .map(dependency -> dependency.get().getName())
                    .collect(Collectors.toList());
        }
    }
}
