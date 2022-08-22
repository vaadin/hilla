package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.NamedModel;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ScanResult;

public final class ScanElementsCollector {
    private final ClassMappers classMappers;
    private List<ClassInfoModel> endpoints;
    private List<ClassInfoModel> entities;
    private String endpointAnnotationName;
    private String endpointExposedAnnotationName;

    public ScanElementsCollector(@Nonnull ScanResult result,
            @Nonnull String endpointAnnotationName,
            @Nonnull String endpointExposedAnnotationName,
            ClassMappers classMappers) {
        this.endpointAnnotationName = Objects
                .requireNonNull(endpointAnnotationName);
        this.endpointExposedAnnotationName = Objects
                .requireNonNull(endpointExposedAnnotationName);
        this.endpoints = Objects.requireNonNull(result)
                .getClassesWithAnnotation(this.endpointAnnotationName).stream()
                .map(ClassInfoModel::of).filter(ClassInfoModel::isNonJDKClass)
                .collect(Collectors.toList());
        this.classMappers = classMappers;
    }

    public ScanElementsCollector collect() {
        entities = endpoints.stream()
                .flatMap(cls -> Streams.combine(cls.getInheritanceChainStream(),
                        cls.getInterfacesStream()))
                .flatMap(cls -> cls.getMethodsStream()
                        .filter(method -> isEndpointExposedMethod(method,
                                endpointAnnotationName,
                                endpointExposedAnnotationName)))
                .flatMap(MethodInfoModel::getDependenciesStream)
                .filter(ClassInfoModel::isNonJDKClass).distinct()
                .map(classMappers::map).filter(ClassInfoModel::isNonJDKClass)
                .distinct().collect(Collectors.toList());

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

            Streams.combine(entity.getFieldDependenciesStream(),
                    entity.getSuperClassStream())
                    .filter(ClassInfoModel::isNonJDKClass).distinct()
                    .map(classMappers::map)
                    .filter(ClassInfoModel::isNonJDKClass)
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

    public static boolean isEndpointExposedMethod(MethodInfoModel method,
            String endpointAnnotatonName,
            String endpointExposedAnnotationName) {
        final ClassInfoModel owner = method.getOwner();
        return hasClassAnnotation(owner, endpointAnnotatonName)
                || hasClassAnnotation(owner, endpointExposedAnnotationName);
    }

    private static boolean hasClassAnnotation(ClassInfoModel classInfoModel,
            String annotationName) {
        return classInfoModel.getAnnotationsStream().map(NamedModel::getName)
                .anyMatch(annotationName::equals);
    }
}
