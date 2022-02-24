package dev.hilla.parser.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;

public final class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final ParserConfig config;
    private final SharedStorage storage;

    public Parser(@Nonnull ParserConfig config) {
        this.config = Objects.requireNonNull(config);
        storage = new SharedStorage(config);
    }

    private static void checkIfJavaCompilerParametersFlagIsEnabled(
            Collection<ClassInfoModel> endpoints) {
        endpoints.stream().flatMap(endpoint -> endpoint.getMethods().stream())
                .flatMap(MethodInfoModel::getParametersStream).findFirst()
                .ifPresent((parameter) -> {
                    if (parameter.getName() == null) {
                        throw new ParserException(
                                "Hilla Parser requires running java compiler with -parameters flag enabled");
                    }
                });
    }

    public OpenAPI execute() {
        logger.debug("Executing JVM Parser");
        var pluginManager = new PluginManager(config, storage);

        var classPathElements = config.getClassPathElements();
        logger.debug("Scanning JVM classpath: "
                + String.join(";", classPathElements));
        var result = new ClassGraph().enableAllInfo().enableExternalClasses()
                .enableSystemJarsAndModules()
                .overrideClasspath(classPathElements).scan();

        var collector = new ElementsCollector(result, logger);
        var endpoints = collector
                .collectEndpoints(config.getEndpointAnnotationName());
        var entities = collector.collectEntities(endpoints);

        logger.debug(
                "Checking if the compiler is run with -parameters option enabled");
        checkIfJavaCompilerParametersFlagIsEnabled(endpoints);

        logger.debug("Executing parser plugins");
        pluginManager.process(endpoints, entities);

        logger.debug("Parsing process successfully finished");
        return storage.getOpenAPI();
    }

    @Nonnull
    public SharedStorage getStorage() {
        return storage;
    }

    private static class ElementsCollector {
        private final Logger logger;
        private final ScanResult result;

        public ElementsCollector(ScanResult result, Logger logger) {
            this.logger = logger;
            this.result = result;
        }

        public Collection<ClassInfoModel> collectEndpoints(
                String endpointAnnotationName) {
            logger.debug(
                    "Collecting project endpoints with the endpoint annotation: "
                            + endpointAnnotationName);

            var endpoints = result
                    .getClassesWithAnnotation(endpointAnnotationName).stream()
                    .map(ClassInfoModel::of)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            logger.debug("Collected project endpoints: "
                    + endpoints.stream().map(ClassInfoModel::getName)
                            .collect(Collectors.joining(", ")));

            return endpoints;
        }

        public Collection<ClassInfoModel> collectEntities(
                Collection<ClassInfoModel> endpoints) {
            var entities = endpoints.stream().flatMap(
                    cls -> cls.getInheritanceChain().getDependenciesStream())
                    .collect(Collectors.toCollection(ArrayList::new));

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

                entity.getDependenciesStream()
                        .filter(e -> !entities.contains(e))
                        .forEach(entities::add);
            }

            logger.debug("Collected project data entities: "
                    + entities.stream().map(ClassInfoModel::getName)
                            .collect(Collectors.joining(", ")));

            return new LinkedHashSet<>(entities);
        }
    }
}
