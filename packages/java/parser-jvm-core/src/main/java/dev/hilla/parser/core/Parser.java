package dev.hilla.parser.core;

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
        var replaceMap = storage.getClassMappers();

        var classPathElements = config.getClassPathElements();
        logger.debug("Scanning JVM classpath: "
                + String.join(";", classPathElements));
        var result = new ClassGraph().enableAllInfo()
                .enableSystemJarsAndModules()
                .overrideClasspath(classPathElements).scan();

        var endpointAnnotationName = config.getEndpointAnnotationName();

        logger.debug(
                "Collecting project endpoints with the endpoint annotation: "
                        + endpointAnnotationName);

        var collector = new ScanElementsCollector(result,
                endpointAnnotationName, replaceMap).collect();

        var endpoints = new LinkedHashSet<>(collector.getEndpoints());

        logger.debug("Collected project endpoints: "
                + endpoints.stream().map(ClassInfoModel::getName)
                        .collect(Collectors.joining(", ")));

        var entities = new LinkedHashSet<>(collector.getEntities());

        logger.debug("Collected project data entities: "
                + entities.stream().map(ClassInfoModel::getName)
                        .collect(Collectors.joining(", ")));

        logger.debug(
                "Checking if the compiler is run with -parameters option enabled");
        checkIfJavaCompilerParametersFlagIsEnabled(collector.getEndpoints());

        logger.debug("Executing parser plugins");
        pluginManager.process(collector);

        logger.debug("Parsing process successfully finished");
        return storage.getOpenAPI();
    }

    @Nonnull
    public SharedStorage getStorage() {
        return storage;
    }
}
