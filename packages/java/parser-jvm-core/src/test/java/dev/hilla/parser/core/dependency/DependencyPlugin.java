package dev.hilla.parser.core.dependency;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

public class DependencyPlugin implements Plugin.Processor {
    public static final String ENTITY_DEPS_STORAGE_KEY = "DependencyPlugin_EntityDeps";
    public static final String DEPS_MEMBERS_STORAGE_KEY = "DependencyPlugin_DepsMembers";
    public static final String ENDPOINTS_DIRECT_DEPS_STORAGE_KEY = "DependencyPlugin_EndpointsDirectDeps";
    private int order = 0;
    private SharedStorage storage;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void process(@Nonnull Collection<ClassInfoModel> endpoints,
            @Nonnull Collection<ClassInfoModel> entities) {
        var collector = new DependencyCollector(endpoints, entities);

        storage.getPluginStorage().put(ENTITY_DEPS_STORAGE_KEY,
                collector.collectEntityDependencyNames());

        storage.getPluginStorage().put(DEPS_MEMBERS_STORAGE_KEY,
                collector.collectDependencyMemberNames());

        storage.getPluginStorage().put(ENDPOINTS_DIRECT_DEPS_STORAGE_KEY,
                collector.collectEndpointDirectDependencyNames());
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }

    private static class DependencyCollector {
        private final Collection<ClassInfoModel> endpoints;
        private final Collection<ClassInfoModel> entities;

        public DependencyCollector(Collection<ClassInfoModel> endpoints,
                Collection<ClassInfoModel> entities) {
            this.endpoints = endpoints;
            this.entities = entities;
        }

        public Collection<String> collectEntityDependencyNames() {
            return entities.stream().map(ClassInfoModel::getName)
                    .collect(Collectors.toList());
        }

        public Collection<String> collectDependencyMemberNames() {
            return entities.stream().flatMap(ClassInfoModel::getFieldsStream)
                    .map(FieldInfoModel::getName).collect(Collectors.toList());
        }

        public Collection<String> collectEndpointDirectDependencyNames() {
            return endpoints.stream()
                    .flatMap(ClassInfoModel::getDependenciesStream)
                    .map(ClassInfoModel::getName).collect(Collectors.toList());
        }
    }
}
