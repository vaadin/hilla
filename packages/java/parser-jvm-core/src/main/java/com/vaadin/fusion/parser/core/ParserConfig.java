package com.vaadin.fusion.parser.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserConfig {

    private String classPath;
    private String endpointAnnotationName;
    private OpenAPI openAPI;
    private Set<String> plugins = new LinkedHashSet<>();

    @Nonnull
    public String getClassPath() {
        return classPath;
    }

    @Nonnull
    public String getEndpointAnnotationName() {
        return endpointAnnotationName;
    }

    @Nonnull
    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    @Nonnull
    public Set<String> getPlugins() {
        return plugins;
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }

        if (!(another instanceof ParserConfig)) {
            return false;
        }

        return Objects.equals(classPath, ((ParserConfig) another).classPath)
                && Objects.equals(endpointAnnotationName,
                        ((ParserConfig) another).endpointAnnotationName)
                && Objects.equals(openAPI, ((ParserConfig) another).openAPI)
                && Objects.equals(plugins, ((ParserConfig) another).plugins);
    }

    public static class Builder {
        private static final Logger logger = LoggerFactory.getLogger(Builder.class);

        private final List<Consumer<ParserConfig>> actions = new ArrayList<>();
        private FileSpec openAPISpec;

        @Nonnull
        public Builder addPlugin(@Nonnull String plugin) {
            Objects.requireNonNull(plugin);
            actions.add(config -> config.plugins.add(plugin));
            return this;
        }

        @Nonnull
        public Builder adjustOpenAPI(@Nonnull Consumer<OpenAPI> action) {
            Objects.requireNonNull(action);

            actions.add(config -> action.accept(config.openAPI));
            return this;
        }

        @Nonnull
        public Builder classPath(@Nonnull String classPath, boolean override) {
            Objects.requireNonNull(classPath);

            actions.add(config -> {
                if (override || config.classPath == null) {
                    config.classPath = classPath;
                }
            });

            return this;
        }

        @Nonnull
        public Builder classPath(@Nonnull String classPath) {
            return classPath(classPath, true);
        }

        @Nonnull
        public Builder endpointAnnotation(
                @Nonnull String annotationFullyQualifiedName,
                boolean override) {
            Objects.requireNonNull(annotationFullyQualifiedName);

            actions.add(config -> {
                if (override || config.endpointAnnotationName == null) {
                    config.endpointAnnotationName = annotationFullyQualifiedName;
                }
            });
            return this;
        }

        @Nonnull
        public Builder endpointAnnotation(
                @Nonnull String annotationQualifiedName) {
            return endpointAnnotation(annotationQualifiedName, true);
        }

        @Nonnull
        public Builder openAPISpec(@Nonnull String src, @Nonnull String ext) {
            openAPISpec = new FileSpec(Objects.requireNonNull(src),
                    Objects.requireNonNull(ext));
            return this;
        }

        @Nonnull
        public Builder plugins(@Nonnull Collection<String> plugins,
                boolean override) {
            Objects.requireNonNull(plugins);

            actions.add(config -> {
                if (override || config.plugins == null) {
                    config.plugins = new LinkedHashSet<>(plugins);
                }
            });
            return this;
        }

        @Nonnull
        public Builder plugins(@Nonnull Collection<String> plugins) {
            return plugins(plugins, true);
        }

        public ParserConfig finish() {
            logger.debug("Building JVM Parser config");
            var config = new ParserConfig();

            logger.debug("Loading OpenAPI configuration");
            config.openAPI = prepareOpenAPI();

            logger.debug("Applying configuration changed defined by the user");
            actions.forEach(action -> action.accept(config));

            Objects.requireNonNull(config.classPath, "[JVM Parser] classPath is not provided");
            Objects.requireNonNull(config.endpointAnnotationName, "[JVM Parser] endpointAnnotationName is not provided");

            return config;
        }

        private OpenAPI prepareOpenAPI() {
            try {
                var parser = new OpenAPIParser();

                var src = new String(Objects
                    .requireNonNull(getClass()
                        .getResourceAsStream("OpenAPIStub.json"))
                    .readAllBytes());

                parser.parse(new FileSpec(src, "json"));

                if (openAPISpec != null) {
                    parser.parse(openAPISpec);
                }

                return parser.getValue();
            } catch (IOException e) {
                throw new ParserException(
                    "Failed to parse openAPI specification", e);
            }
        }
    }

    private static class FileSpec {
        private final String src;
        private final String extension;

        public FileSpec(String src, String extension) {
            this.src = src;
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }

        public String getSrc() {
            return src;
        }
    }

    private static final class OpenAPIParser {
        private OpenAPI value;

        public OpenAPI getValue() {
            return value;
        }

        public void parse(FileSpec spec) throws IOException {
            var mapper = createMapper(spec.getExtension());
            var reader = value != null ? mapper.readerForUpdating(value)
                    : mapper.reader();
            value = reader.readValue(spec.getSrc(), OpenAPI.class);
        }

        private ObjectMapper createMapper(String extension) {
            switch (extension) {
            case "yml":
            case "yaml":
                return Yaml.mapper();
            case "json":
                return Json.mapper();
            default:
                throw new ParserException(String.format(
                        "The file format '.%s' is not supported", extension));
            }
        }
    }
}
