package com.vaadin.fusion.parser.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

public final class ParserConfig extends AbstractParserConfig {
    private String classPath;
    private String endpointAnnotationName;
    private OpenAPI openAPI;
    private Set<String> plugins = new LinkedHashSet<>();

    private ParserConfig() {
    }

    @Nonnull
    @Override
    public String getClassPath() {
        return classPath;
    }

    @Nonnull
    @Override
    public String getEndpointAnnotationName() {
        return endpointAnnotationName;
    }

    @Nonnull
    @Override
    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    @Nonnull
    @Override
    public Set<String> getPlugins() {
        return Collections.unmodifiableSet(plugins);
    }

    public enum OpenAPIFileType {
        JSON(Json.mapper()), YAML(Yaml.mapper());

        private final ObjectMapper mapper;

        OpenAPIFileType(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public ObjectMapper getMapper() {
            return mapper;
        }
    }

    public static final class Builder {
        private static final Logger logger = LoggerFactory
                .getLogger(Builder.class);

        private final List<Consumer<ParserConfig>> actions = new ArrayList<>();
        private FileSource openAPISpec;

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
        public Builder openAPISource(@Nonnull String src,
                @Nonnull OpenAPIFileType type) {
            openAPISpec = new FileSource(Objects.requireNonNull(src),
                    Objects.requireNonNull(type));
            return this;
        }

        @Nonnull
        public Builder plugins(@Nonnull Collection<String> plugins) {
            Objects.requireNonNull(plugins);
            actions.add(config -> config.plugins = new LinkedHashSet<>(plugins));
            return this;
        }

        public ParserConfig finish() {
            logger.debug("Building JVM Parser config.");
            var config = new ParserConfig();

            logger.debug("Loading OpenAPI configuration.");
            config.openAPI = prepareOpenAPI();

            logger.debug("Applying configuration changed defined by the user.");
            actions.forEach(action -> action.accept(config));

            Objects.requireNonNull(config.classPath,
                    "[JVM Parser] classPath is not provided.");
            Objects.requireNonNull(config.endpointAnnotationName,
                    "[JVM Parser] endpointAnnotationName is not provided.");

            return config;
        }

        private OpenAPI prepareOpenAPI() {
            try {
                var parser = new OpenAPIParser();

                var src = new String(Objects
                        .requireNonNull(getClass()
                                .getResourceAsStream("OpenAPIBase.json"))
                        .readAllBytes());

                parser.parse(new FileSource(src, OpenAPIFileType.JSON));

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

    private static final class FileSource {
        private final String src;
        private final OpenAPIFileType type;

        public FileSource(String src, OpenAPIFileType type) {
            this.src = src;
            this.type = type;
        }

        public OpenAPIFileType getType() {
            return type;
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

        public void parse(FileSource spec) throws IOException {
            var mapper = spec.getType().getMapper();
            var reader = value != null ? mapper.readerForUpdating(value)
                    : mapper.reader();
            value = reader.readValue(spec.getSrc(), OpenAPI.class);
        }
    }
}
