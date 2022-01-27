package dev.hilla.parser.core.dependency;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.RelativeClassInfo;
import dev.hilla.parser.core.RelativeFieldInfo;
import dev.hilla.parser.core.RelativeMethodInfo;
import dev.hilla.parser.core.SharedStorage;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

public class DependencyPlugin implements Plugin {
    public static final String ALL_DEPS_STORAGE_KEY = "DependencyPlugin_AllDeps";
    public static final String DEPS_MEMBERS_STORAGE_KEY = "DependencyPlugin_DepsMembers";
    public static final String ENDPOINTS_DIRECT_DEPS_STORAGE_KEY = "DependencyPlugin_EndpointsDirectDeps";
    private int order = 0;

    @Override
    public void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            SharedStorage storage) {
        var collector = new DependencyCollector(endpoints, entities);

        storage.getPluginStorage().put(ALL_DEPS_STORAGE_KEY,
                collector.collectAllDependencyNames());

        storage.getPluginStorage().put(DEPS_MEMBERS_STORAGE_KEY,
                collector.collectDependencyMemberNames());

        storage.getPluginStorage().put(ENDPOINTS_DIRECT_DEPS_STORAGE_KEY,
                collector.collectEndpointDirectDependencyNames());
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    private static class DependencyCollector {
        private final Collection<RelativeClassInfo> endpoints;
        private final Collection<RelativeClassInfo> entities;

        public DependencyCollector(Collection<RelativeClassInfo> endpoints,
                Collection<RelativeClassInfo> entities) {
            this.endpoints = endpoints;
            this.entities = entities;
        }

        public List<String> collectAllDependencyNames() {
            return entities.stream().map(RelativeClassInfo::get)
                    .map(ClassInfo::getName).collect(Collectors.toList());
        }

        public List<String> collectDependencyMemberNames() {
            return Stream.of(entities.stream()
                    .flatMap(RelativeClassInfo::getFieldsStream)
                    .map(RelativeFieldInfo::get).map(FieldInfo::getName),
                    entities.stream()
                            .flatMap(RelativeClassInfo::getMethodsStream)
                            .map(RelativeMethodInfo::get)
                            .map(MethodInfo::getName),
                    entities.stream()
                            .flatMap(RelativeClassInfo::getInnerClassesStream)
                            .map(RelativeClassInfo::get)
                            .map(ClassInfo::getName))
                    .flatMap(Function.identity()).collect(Collectors.toList());
        }

        public List<String> collectEndpointDirectDependencyNames() {
            return endpoints.stream()
                    .flatMap(RelativeClassInfo::getDependenciesStream)
                    .map(RelativeClassInfo::get).map(ClassInfo::getName)
                    .collect(Collectors.toList());
        }
    }
}
