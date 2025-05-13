package com.vaadin.hilla.engine;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineAutoConfiguration {
    private static EngineAutoConfiguration INSTANCE;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EngineAutoConfiguration.class);

    public static final String OPEN_API_PATH = "hilla-openapi.json";
    private Set<Path> classpath = Arrays
            .stream(System.getProperty("java.class.path")
                    .split(File.pathSeparator))
            .map(Path::of).collect(Collectors.toSet());
    private String groupId;
    private String artifactId;
    private String mainClass;
    private Path buildDir;
    private Path baseDir;
    private List<Path> classesDirs;
    private GeneratorConfiguration generator;
    private Path outputDir;
    private ParserConfiguration parser;
    private BrowserCallableFinder browserCallableFinder;
    private boolean productionMode = false;
    private String nodeCommand = "node";
    private ClassFinder classFinder;
    private ClassLoader classLoader;
    private EngineConfiguration userEngineConfiguration;

    private EngineAutoConfiguration() {
        baseDir = Path.of(System.getProperty("user.dir"));
        buildDir = baseDir.resolve("target");
        generator = new GeneratorConfiguration();
        parser = new ParserConfiguration();

        var legacyFrontendDir = baseDir.resolve("frontend");
        if (Files.exists(legacyFrontendDir)) {
            outputDir = legacyFrontendDir.resolve("generated");
        } else {
            outputDir = baseDir.resolve(
                    FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR);
        }
    }

    public Set<Path> getClasspath() {
        return classpath;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public Path getBuildDir() {
        return buildDir;
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public List<Path> getClassesDirs() {
        return classesDirs == null ? List.of(buildDir.resolve("classes"))
                : classesDirs;
    }

    public GeneratorConfiguration getGenerator() {
        return generator;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public ParserConfiguration getParser() {
        return parser;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public String getNodeCommand() {
        return nodeCommand;
    }

    public ClassFinder getClassFinder() {
        return classFinder;
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null && getClassFinder() != null) {
            classLoader = getClassFinder().getClassLoader();
        }

        if (classLoader == null) {
            var urls = getClasspath().stream().map(path -> {
                try {
                    return path.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new ConfigurationException(
                            "Classpath contains invalid elements", e);
                }
            }).toArray(URL[]::new);
            classLoader = new URLClassLoader(urls, getClass().getClassLoader());
        }

        return getUserEngineConfiguration().getClassLoader(classLoader);
    }

    public List<Class<? extends Annotation>> getEndpointAnnotations() {
        return parser.getEndpointAnnotations();
    }

    public List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
        return parser.getEndpointExposedAnnotations();
    }

    public Path getOpenAPIFile() {
        return productionMode
                ? buildDir.resolve("classes").resolve(OPEN_API_PATH)
                : buildDir.resolve(OPEN_API_PATH);
    }

    public BrowserCallableFinder getBrowserCallableFinder() {
        BrowserCallableFinder result = browserCallableFinder;

        if (result == null) {
            result = (conf) -> {
                var exceptions = new ArrayList<BrowserCallableFinderException>();

                return Stream.<BrowserCallableFinder> of(
                        LookupBrowserCallableFinder::find,
                        AotBrowserCallableFinder::find).map(finder -> {
                            try {
                                return finder.find(conf);
                            } catch (BrowserCallableFinderException e) {
                                exceptions.add(e);
                                return null;
                            }
                        }).filter(Objects::nonNull).findFirst()
                        .orElseThrow(() -> {
                            if (exceptions.isEmpty()) {
                                return new ParserException(
                                        "No browser-callable classes found");
                            } else {
                                var exception = new ParserException(
                                        "Failed to find browser-callable classes");
                                exceptions.forEach(exception::addSuppressed);
                                return exception;
                            }
                        });
            };
        }

        return getUserEngineConfiguration().getBrowserCallableFinder(result);
    }

    private EngineConfiguration getUserEngineConfiguration() {
        if (userEngineConfiguration == null) {
            userEngineConfiguration = load();
        }
        return userEngineConfiguration;
    }

    public static EngineAutoConfiguration getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new EngineAutoConfiguration();
        }

        return INSTANCE;
    }

    public static void setDefault(EngineAutoConfiguration config) {
        INSTANCE = config;
    }

    /**
     * Loads the customized configuration using the Java ServiceLoader
     * mechanism.
     */
    static EngineConfiguration load() {
        return pick(ServiceLoader.load(EngineConfiguration.class).stream()
                .map(ServiceLoader.Provider::get)
                .toArray(EngineConfiguration[]::new));
    }

    /**
     * Picks the first customized configuration from the provided array. If
     * there are no configurations, a default one is returned. If there are
     * multiple configurations, an exception is thrown.
     *
     * @param configurations
     *            the configurations to pick from
     * @return the picked configuration
     */
    static EngineConfiguration pick(EngineConfiguration... configurations) {
        return switch (configurations.length) {
        case 0 -> new EngineConfiguration() {
        };
        case 1 -> configurations[0];
        default -> throw new ConfigurationException(Arrays
                .stream(configurations)
                .map(config -> config.getClass().getName())
                .collect(Collectors.joining("\", \"",
                        "Multiple EngineConfiguration implementations found: \"",
                        "\"")));
        };
    }

    public static final class Builder {
        private final EngineAutoConfiguration configuration = new EngineAutoConfiguration();

        public Builder() {
            this(getDefault());
        }

        public Builder(EngineAutoConfiguration configuration) {
            this.configuration.baseDir = configuration.baseDir;
            this.configuration.buildDir = configuration.buildDir;
            this.configuration.classesDirs = configuration.classesDirs;
            this.configuration.classpath = configuration.classpath;
            this.configuration.generator = configuration.generator;
            this.configuration.parser = configuration.parser;
            this.configuration.outputDir = configuration.outputDir;
            this.configuration.groupId = configuration.groupId;
            this.configuration.artifactId = configuration.artifactId;
            this.configuration.mainClass = configuration.mainClass;
            this.configuration.browserCallableFinder = configuration.browserCallableFinder;
            this.configuration.productionMode = configuration.productionMode;
            this.configuration.nodeCommand = configuration.nodeCommand;
            this.configuration.classFinder = configuration.classFinder;
            this.configuration.classLoader = configuration.classLoader;
            this.configuration.parser.setEndpointAnnotations(
                    configuration.getEndpointAnnotations());
            this.configuration.parser.setEndpointExposedAnnotations(
                    configuration.getEndpointExposedAnnotations());
        }

        public Builder baseDir(Path value) {
            configuration.baseDir = value;
            return this;
        }

        public Builder buildDir(String value) {
            return buildDir(Path.of(value));
        }

        public Builder buildDir(Path value) {
            configuration.buildDir = resolve(value);
            return this;
        }

        public Builder classesDirs(List<Path> values) {
            configuration.classesDirs = values.stream().map(this::resolve)
                    .toList();
            return this;
        }

        public Builder classpath(Collection<String> value) {
            configuration.classpath = value.stream().map(Path::of)
                    .map(this::resolve).collect(Collectors.toSet());
            return this;
        }

        public EngineAutoConfiguration build() {
            return configuration;
        }

        public Builder generator(GeneratorConfiguration value) {
            configuration.generator = value;
            return this;
        }

        public Builder outputDir(String value) {
            return outputDir(Path.of(value));
        }

        public Builder outputDir(Path value) {
            configuration.outputDir = resolve(value);
            return this;
        }

        public Builder parser(ParserConfiguration value) {
            configuration.parser = value;
            return this;
        }

        public Builder groupId(String value) {
            configuration.groupId = value;
            return this;
        }

        public Builder artifactId(String value) {
            configuration.artifactId = value;
            return this;
        }

        public Builder mainClass(String value) {
            configuration.mainClass = value;
            return this;
        }

        public Builder browserCallableFinder(BrowserCallableFinder finder) {
            configuration.browserCallableFinder = finder;
            return this;
        }

        public Builder productionMode(boolean value) {
            configuration.productionMode = value;
            return this;
        }

        public Builder nodeCommand(String value) {
            configuration.nodeCommand = value;
            return this;
        }

        public Builder classFinder(ClassFinder value) {
            configuration.classFinder = value;
            return this;
        }

        public Builder classLoader(ClassLoader value) {
            configuration.classLoader = value;
            return this;
        }

        public Builder endpointAnnotations(
                Class<? extends Annotation>... value) {
            configuration.parser.setEndpointAnnotations(Arrays.asList(value));
            return this;
        }

        public Builder endpointExposedAnnotations(
                Class<? extends Annotation>... value) {
            configuration.parser
                    .setEndpointExposedAnnotations(Arrays.asList(value));
            return this;
        }

        public Builder withDefaultAnnotations() {
            ClassLoader classLoader = getClass().getClassLoader();
            if (configuration.classpath != null) {
                var urls = configuration.classpath.stream().map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new ConfigurationException(
                                "Classpath contains invalid elements", e);
                    }
                }).toArray(URL[]::new);
                classLoader = new URLClassLoader(urls,
                        getClass().getClassLoader());
            }

            try {
                configuration.parser.setEndpointAnnotations(List.of(
                        (Class<? extends Annotation>) Class.forName(
                                "com.vaadin.hilla.BrowserCallable", true,
                                classLoader),
                        (Class<? extends Annotation>) Class.forName(
                                "com.vaadin.hilla.Endpoint", true,
                                classLoader)));
                configuration.parser.setEndpointExposedAnnotations(
                        List.of((Class<? extends Annotation>) Class.forName(
                                "com.vaadin.hilla.EndpointExposed", true,
                                classLoader)));
            } catch (Throwable t) {
                LOGGER.debug(
                        "Default annotations not found. Hilla is probably not in the classpath.");
            }
            return this;
        }

        private Path resolve(Path path) {
            return path.isAbsolute() ? path.normalize()
                    : configuration.baseDir.resolve(path).normalize();
        }
    }
}
