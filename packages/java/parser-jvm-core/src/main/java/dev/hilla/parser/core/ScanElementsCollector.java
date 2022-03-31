package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;

import io.github.classgraph.ScanResult;

public final class ScanElementsCollector {
    private final ReplaceMap replaceMap;
    private List<ClassInfoModel> endpoints;
    private List<ClassInfoModel> entities;

    public ScanElementsCollector(@Nonnull ScanResult result,
            @Nonnull String endpointAnnotationName, ReplaceMap replaceMap) {
        this(Objects.requireNonNull(result)
                .getClassesWithAnnotation(
                        Objects.requireNonNull(endpointAnnotationName))
                .stream().map(ClassInfoModel::of).collect(Collectors.toList()),
                replaceMap);
    }

    public ScanElementsCollector(@Nonnull Collection<ClassInfoModel> endpoints,
            ReplaceMap replaceMap) {
        this.endpoints = Objects.requireNonNull(endpoints) instanceof ArrayList
                ? (ArrayList<ClassInfoModel>) endpoints
                : new ArrayList<>(endpoints);
        this.replaceMap = replaceMap;
    }

    public ScanElementsCollector collect() {
        endpoints = endpoints.stream().map(replaceMap::replace)
                .collect(Collectors.toList());

        entities = endpoints.stream()
                .flatMap(cls -> cls.getInheritanceChain()
                        .getDependenciesStream())
                .map(replaceMap::replace).distinct()
                .collect(Collectors.toList());

        // @formatter:off
        //
        // ATTENTION: This loop changes collection during iteration!
        // The reason is the following:
        // - Each entity can have multiple dependencies.
        // - Each dependency can have multiple dependencies.
        // - The `getDependenciesStream` method provides only the list of
        // direct dependencies of the entity.
        //
        // All bullets above mean that `getDependenciesStream` should be
        // called recursively on each entity and their dependencies.
        //
        // We need to collect everything in a single collection. The most
        // straightforward way is to  add all dependencies from the
        // `getDependenciesStream` to the same collection we iterate right
        // now. Then, the loop will eventually reach them and collect
        // their dependencies as well.
        //
        // The algorithm looks like the following:
        //
        // == Initialize the collection
        //   var entities = [entity1, entity2];
        //
        // == First iteration:
        //   entity1.getDependenciesStream() -> [dep1, dep2]
        // Let's add it to our collection:
        //   [entity1, entity2, dep1, dep2]
        //               ^
        //          the next element
        //
        // == Second iteration
        //   entity2.getDependenciesStream() -> [dep3, dep4]
        // Let's add it to our collection:
        //   [entity1, entity2, dep1, dep2, dep3, dep4]
        //                       ^
        //                  the next element
        //
        // == Other iterations
        // Now repeat the algorithm until there is no dependency that does
        // not already exist in our collection.
        //
        // @formatter:on
        for (var i = 0; i < entities.size(); i++) {
            var entity = entities.get(i);

            entity.getDependenciesStream().map(replaceMap::replace)
                    .filter(e -> !entities.contains(e)).forEach(entities::add);
        }

        return this;
    }

    public List<ClassInfoModel> getEndpoints() {
        return endpoints;
    }

    public List<ClassInfoModel> getEntities() {
        return entities;
    }
}
