package dev.hilla.parser.core;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public OpenAPI execute() {
        logger.debug("Executing JVM Parser");

        try (var scanResult = new ClassGraph().enableAllInfo()
                .enableSystemJarsAndModules()
                .overrideClasspath(config.getClassPathElements()).scan()) {
            var rootNode = new RootNode(scanResult, storage.getOpenAPI());
            var pluginManager = new PluginManager(
                    storage.getParserConfig().getPlugins());
            pluginManager.setStorage(storage);
            var pluginExecutor = new PluginExecutor(pluginManager, rootNode);
            pluginExecutor.execute();
        }

        return storage.getOpenAPI();
    }

    @Nonnull
    public SharedStorage getStorage() {
        return storage;
    }
}
