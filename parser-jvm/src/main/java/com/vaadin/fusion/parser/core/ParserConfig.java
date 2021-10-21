package com.vaadin.fusion.parser.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

public final class ParserConfig {
    private final Application application = new Application();
    private final Plugins plugins = new Plugins();
    private String classPath;
    @JsonIgnore
    private OpenAPI openAPI;

    ParserConfig() {
    }

    @Nonnull
    public Application getApplication() {
        return application;
    }

    @Nonnull
    public String getClassPath() {
        return classPath;
    }

    @Nonnull
    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    @Nonnull
    public Plugins getPlugins() {
        return plugins;
    }

    public static final class Application {
        private String endpointAnnotation;

        private Application() {
        }

        public String getEndpointAnnotation() {
            return endpointAnnotation;
        }
    }

    public static final class Builder {
        private final List<Consumer<ParserConfig>> actions = new ArrayList<>();
        private File configFile;
        private File openAPITemplate;

        public Builder() {
        }

        @Nonnull
        public Builder adjustOpenAPI(@Nonnull Consumer<OpenAPI> action) {
            Objects.requireNonNull(action);

            actions.add(config -> action.accept(config.openAPI));
            return this;
        }

        @Nonnull
        public Builder classPath(@Nonnull String classPath) {
            return classPath(classPath, true);
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
        public Builder configFile(@Nonnull File file) {
            configFile = Objects.requireNonNull(file);
            return this;
        }

        @Nonnull
        public Builder disableDefaultPlugin(@Nonnull String plugin) {
            Objects.requireNonNull(plugin);

            actions.add(config -> config.plugins.disable.add(plugin));
            return this;
        }

        @Nonnull
        public Builder disableDefaultPlugins(
                @Nonnull Collection<String> plugins) {
            return disableDefaultPlugins(plugins, true);
        }

        @Nonnull
        public Builder disableDefaultPlugins(
                @Nonnull Collection<String> plugins, boolean override) {
            Objects.requireNonNull(plugins);

            actions.add(config -> {
                if (override || config.plugins.disable == null) {
                    config.plugins.disable = new LinkedHashSet<>(plugins);
                }
            });
            return this;
        }

        @Nonnull
        public Builder disableDefaultPlugins(@Nonnull String... plugins) {
            return disableDefaultPlugins(Arrays.asList(plugins));
        }

        @Nonnull
        public Builder endpointAnnotation(
                @Nonnull String annotationQualifiedName) {
            return endpointAnnotation(annotationQualifiedName, true);
        }

        @Nonnull
        public Builder endpointAnnotation(
                @Nonnull String annotationQualifiedName, boolean override) {
            Objects.requireNonNull(annotationQualifiedName);

            actions.add(config -> {
                if (override || config.application.endpointAnnotation == null) {
                    config.application.endpointAnnotation = annotationQualifiedName;
                }
            });
            return this;
        }

        @Nonnull
        public ParserConfig finish() {
            Loader<ParserConfig> configLoader = new Loader<>(
                    ParserConfig.class);
            Loader<OpenAPI> openAPILoader = new Loader<>(OpenAPI.class);

            configLoader.load(Objects.requireNonNull(
                    getClass().getResource("ParserConfigStub.json")));
            openAPILoader.load(Objects.requireNonNull(
                    getClass().getResource("OpenAPIStub.json")));

            if (configFile != null) {
                configLoader.load(configFile);
            }

            if (openAPITemplate != null) {
                configLoader.load(openAPITemplate);
            }

            ParserConfig config = configLoader.getValue();
            config.openAPI = openAPILoader.getValue();

            for (Consumer<ParserConfig> action : actions) {
                action.accept(config);
            }

            Objects.requireNonNull(config.classPath,
                    "Fusion Parser Configuration: Classpath is not provided.");

            return config;
        }

        @Nonnull
        public Builder openAPITemplate(@Nonnull File file) {
            openAPITemplate = Objects.requireNonNull(file);
            return this;
        }

        @Nonnull
        public Builder usePlugin(@Nonnull String plugin) {
            Objects.requireNonNull(plugin);

            actions.add(config -> config.plugins.use.add(plugin));
            return this;
        }

        @Nonnull
        public Builder usePlugins(@Nonnull Collection<String> plugins) {
            return usePlugins(plugins, true);
        }

        @Nonnull
        public Builder usePlugins(@Nonnull Collection<String> plugins,
                boolean override) {
            Objects.requireNonNull(plugins);

            actions.add(config -> {
                if (override || config.plugins.use == null) {
                    config.plugins.use = new LinkedHashSet<>(plugins);
                }
            });
            return this;
        }

        @Nonnull
        public Builder usePlugins(@Nonnull String... plugins) {
            return usePlugins(Arrays.asList(plugins));
        }
    }

    public static final class Plugins {
        @JsonDeserialize(as = LinkedHashSet.class)
        private Set<String> disable;

        @JsonDeserialize(as = LinkedHashSet.class)
        private Set<String> use;

        private Plugins() {
        }

        @Nonnull
        public Set<String> getDisable() {
            return Collections.unmodifiableSet(disable);
        }

        @Nonnull
        public Set<String> getUse() {
            return Collections.unmodifiableSet(use);
        }
    }

    private static final class Loader<T> {
        private final Class<T> type;
        private T value;

        public Loader(Class<T> type) {
            this.type = type;
        }

        public T getValue() {
            return value;
        }

        public void load(URL url) {
            try {
                load(new File(url.toURI()));
            } catch (URISyntaxException e) {
                throw new ParserException(e);
            }
        }

        public void load(File file) {
            try {
                ObjectMapper mapper = createMapper(file);
                ObjectReader reader = value != null
                        ? mapper.readerForUpdating(value)
                        : mapper.reader();

                value = reader.readValue(file, type);
            } catch (IOException e) {
                throw new ParserException("Failed to parse configuration file",
                        e);
            }
        }

        private ObjectMapper createMapper(File file) {
            String extension = FilenameUtils.getExtension(file.toString());

            switch (extension) {
            case "yml":
            case "yaml":
                return type == OpenAPI.class ? Yaml.mapper()
                        : new ObjectMapper(new YAMLFactory());
            case "json":
                return type == OpenAPI.class ? Json.mapper()
                        : new ObjectMapper(new JsonFactory());
            default:
                throw new ParserException(String.format(
                        "The file format '.%s' is not supported", extension));
            }
        }
    }
}
