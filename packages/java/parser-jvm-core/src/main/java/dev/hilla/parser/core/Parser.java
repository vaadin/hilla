package dev.hilla.parser.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.node.Node;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;
import dev.hilla.parser.node.RootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

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

        var rootNode = new RootNode(storage.getParserConfig(), storage.getOpenAPI());
        var pluginManager = new PluginManager(storage);
        var pluginExecutor = new PluginExecutor(pluginManager, rootNode);
        pluginExecutor.execute();

        return storage.getOpenAPI();
    }

    @Nonnull
    public SharedStorage getStorage() {
        return storage;
    }
}
