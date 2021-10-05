package com.vaadin.fusion.parser.dependency;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.fusion.parser.Plugin;
import com.vaadin.fusion.parser.RelativeClassList;
import com.vaadin.fusion.parser.SharedStorage;
import com.vaadin.fusion.parser.TestUtils;

public class DependencyPlugin implements Plugin {
    public static final String ALL_DEPS_STORAGE_KEY = "DependencyPlugin_AllDeps";
    public static final String DEPS_MEMBERS_STORAGE_KEY = "DependencyPlugin_DepsMembers";
    public static final String ENDPOINTS_DIRECT_DEPS_STORAGE_KEY = "DependencyPlugin_EndpointsDirectDeps";

    protected final TestUtils.PluginElementsFilter filter = new TestUtils.PluginElementsFilter(
            "Dependency");

    @Override
    public void execute(RelativeClassList endpoints, RelativeClassList entities,
            SharedStorage storage) {
        DependencyCollector collector = new DependencyCollector(
                filter.apply(endpoints), filter.apply(entities));

        storage.getPluginStorage().put(ALL_DEPS_STORAGE_KEY,
                collector.collectAllDependencyNames());

        storage.getPluginStorage().put(DEPS_MEMBERS_STORAGE_KEY,
                collector.collectDependencyMemberNames());

        storage.getPluginStorage().put(ENDPOINTS_DIRECT_DEPS_STORAGE_KEY,
                collector.collectEndpointDirectDependencyNames());
    }

    private static class DependencyCollector {
        private final RelativeClassList endpoints;
        private final RelativeClassList entities;

        DependencyCollector(RelativeClassList endpoints,
                RelativeClassList entities) {
            this.endpoints = endpoints;
            this.entities = entities;
        }

        List<String> collectAllDependencyNames() {
            return entities.stream()
                    .map(dependency -> dependency.get().getName())
                    .collect(Collectors.toList());
        }

        List<String> collectDependencyMemberNames() {
            return Stream
                    .of(entities.streamRelative().getFields()
                            .map(field -> field.get().getName()),
                            entities.streamRelative().getMethods()
                                    .map(method -> method.get().getName()),
                            entities.streamRelative().getInnerClasses().stream()
                                    .map(cls -> cls.get().getName()))
                    .flatMap(Function.identity()).collect(Collectors.toList());
        }

        List<String> collectEndpointDirectDependencyNames() {
            return endpoints.stream()
                    .flatMap(endpoint -> endpoint.getDependencies().stream())
                    .map(dependency -> dependency.get().getName())
                    .collect(Collectors.toList());
        }
    }
}
