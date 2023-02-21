package dev.hilla.engine;

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

import dev.hilla.parser.utils.OpenAPIPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.hilla.parser.core.OpenAPIFileType;
import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.PluginManager;

public final class ParserProcessor {
    private static final Logger logger = LoggerFactory
            .getLogger(ParserProcessor.class);
    private final Path baseDir;
    private final ParserConfiguration.PluginsProcessor pluginsProcessor = new ParserConfiguration.PluginsProcessor();
    private final ClassLoader classLoader;
    private final Set<String> classPath;
    private String endpointAnnotationName = "dev.hilla.Endpoint";
    private String endpointExposedAnnotationName = "dev.hilla.EndpointExposed";
    private Collection<String> exposedPackages = List.of();
    private String openAPIBasePath;

    private final Path openAPIFile;

    public ParserProcessor(EngineConfiguration conf, ClassLoader classLoader) {
        this.baseDir = conf.getBaseDir();
        this.openAPIFile = conf.getOpenAPIFile();
        this.classLoader = classLoader;
        this.classPath = conf.getClassPath();
        applyConfiguration(conf.getParser());
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

    public void process() throws ParserException {
        var parser = new Parser().classLoader(classLoader).classPath(classPath)
                .endpointAnnotation(endpointAnnotationName)
                .endpointExposedAnnotation(endpointExposedAnnotationName)
                .exposedPackages(exposedPackages);

        preparePlugins(parser);
        prepareOpenAPIBase(parser);

        logger.debug("Starting JVM Parser");

        var openAPI = parser.execute();

        logger.debug("Saving OpenAPI file to " + openAPIFile);

        String openAPIString;

        try {
            Files.createDirectories(openAPIFile.getParent());
            openAPIString = new OpenAPIPrinter().pretty()
                    .writeAsString(openAPI);
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

    // Workaround for IOException in lambda
    private String readFromFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            logger.error("Unable to read file", e);
            return null;
        }
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

    private void applyOpenAPIBase(@Nonnull String openAPIBasePath) {
        this.openAPIBasePath = openAPIBasePath;
    }

    private void applyPlugins(@Nonnull ParserConfiguration.Plugins plugins) {
        this.pluginsProcessor.setConfig(plugins);
    }

    private void applyExposedPackages(@Nonnull List<String> exposedPackages) {
        this.exposedPackages = exposedPackages;
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
}
