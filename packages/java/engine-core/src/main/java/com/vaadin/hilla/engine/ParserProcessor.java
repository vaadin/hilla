package com.vaadin.hilla.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.parser.core.OpenAPIFileType;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.PluginManager;
import com.vaadin.hilla.parser.utils.JsonPrinter;

import static com.vaadin.hilla.engine.EngineConfiguration.OPEN_API_PATH;

public final class ParserProcessor {
    private static final Logger logger = LoggerFactory
            .getLogger(ParserProcessor.class);
    private final Path baseDir;
    private final ClassLoader classLoader;
    private final Set<Path> classPath;
    private final Path openAPIFile;
    private final ParserConfiguration.PluginsProcessor pluginsProcessor = new ParserConfiguration.PluginsProcessor();
    private String endpointAnnotationName = "com.vaadin.hilla.Endpoint";
    private String endpointExposedAnnotationName = "com.vaadin.hilla.EndpointExposed";
    private Collection<String> exposedPackages = List.of();
    private String openAPIBasePath;

    public ParserProcessor(EngineConfiguration conf, ClassLoader classLoader,
            boolean isProductionMode) {
        this.baseDir = conf.getBaseDir();
        this.openAPIFile = conf.getOpenAPIFile(isProductionMode);
        this.classLoader = classLoader;
        this.classPath = conf.getClassPath();
        applyConfiguration(conf.getParser());
    }

    public String createOpenAPI() throws IOException {
        var parser = new Parser().classLoader(classLoader)
                .classPath(classPath.stream().map(Path::toString)
                        .collect(Collectors.toSet()))
                .endpointAnnotation(endpointAnnotationName)
                .endpointExposedAnnotation(endpointExposedAnnotationName)
                .exposedPackages(exposedPackages);

        preparePlugins(parser);
        prepareOpenAPIBase(parser);

        logger.debug("Starting JVM Parser");

        var openAPI = parser.execute();

        return new JsonPrinter().pretty().writeAsString(openAPI);
    }

    public void process() throws ParserException {
        String openAPIString;

        try {
            Files.createDirectories(openAPIFile.getParent());
            openAPIString = createOpenAPI();
        } catch (IOException e) {
            throw new ParserException("Unable to prepare OpenAPI definition",
                    e);
        }

        // Only save the file if it has changed
        Optional.of(openAPIFile).filter(Files::isRegularFile)
                .map(this::readFromFile).filter(openAPIString::equals)
                .ifPresentOrElse(s -> {
                    logger.debug("OpenAPI definition has not changed");
                }, () -> {
                    try {
                        Files.write(openAPIFile, openAPIString.getBytes());
                        logger.debug("OpenAPI definition file saved");
                    } catch (IOException e) {
                        throw new ParserException("Unable to save OpenAPI file",
                                e);
                    }
                });
    }

    private void applyConfiguration(ParserConfiguration parserConfiguration) {
        if (parserConfiguration == null) {
            return;
        }

        parserConfiguration.getEndpointAnnotation()
                .ifPresent(this::applyEndpointAnnotation);
        parserConfiguration.getEndpointExposedAnnotation()
                .ifPresent(this::applyEndpointExposedAnnotation);
        parserConfiguration.getOpenAPIBasePath()
                .ifPresent(this::applyOpenAPIBase);
        parserConfiguration.getPlugins().ifPresent(this::applyPlugins);
        parserConfiguration.getPackages().ifPresent(this::applyExposedPackages);
    }

    private void applyEndpointAnnotation(
            @Nonnull String endpointAnnotationName) {
        this.endpointAnnotationName = Objects
                .requireNonNull(endpointAnnotationName);
    }

    private void applyEndpointExposedAnnotation(
            @Nonnull String endpointExposedAnnotationName) {
        this.endpointExposedAnnotationName = Objects
                .requireNonNull(endpointExposedAnnotationName);
    }

    private void applyExposedPackages(@Nonnull List<String> exposedPackages) {
        this.exposedPackages = exposedPackages;
    }

    private void applyOpenAPIBase(@Nonnull String openAPIBasePath) {
        this.openAPIBasePath = openAPIBasePath;
    }

    private void applyPlugins(@Nonnull ParserConfiguration.Plugins plugins) {
        this.pluginsProcessor.setConfig(plugins);
    }

    private void prepareOpenAPIBase(Parser parser) {
        if (openAPIBasePath == null) {
            return;
        }

        try {
            var path = baseDir.resolve(openAPIBasePath);
            var fileName = path.getFileName().toString();

            if (!fileName.endsWith("yml") && !fileName.endsWith("yaml")
                    && !fileName.endsWith("json")) {
                throw new IOException("No OpenAPI base file found");
            }

            parser.openAPISource(Files.readString(path),
                    fileName.endsWith("json") ? OpenAPIFileType.JSON
                            : OpenAPIFileType.YAML);
        } catch (IOException e) {
            throw new ParserException("Failed loading OpenAPI spec file", e);
        }
    }

    private void preparePlugins(Parser parser) {
        var loadedPlugins = pluginsProcessor.process().stream()
                .map((plugin) -> PluginManager.load(plugin.getName(),
                        plugin.getConfiguration()))
                .collect(Collectors.toList());

        parser.plugins(loadedPlugins);
    }

    // Workaround for IOException in lambda
    private String readFromFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            logger.error("Unable to read file", e);
            return null;
        }
    }
}
