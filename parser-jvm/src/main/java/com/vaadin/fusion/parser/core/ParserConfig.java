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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
            Mapper<ParserConfig> configMapper = new Mapper<>(
                    ParserConfig.class);
            Mapper<OpenAPI> openAPIMapper = new Mapper<>(OpenAPI.class);

            ParserConfig config = configFile == null
                    ? configMapper.map(
                            getClass().getResource("ParserConfigStub.json"))
                    : configMapper.map(configFile);

            config.openAPI = openAPITemplate == null
                    ? openAPIMapper
                            .map(getClass().getResource("OpenAPIStub.json"))
                    : openAPIMapper.map(openAPITemplate);

            for (Consumer<ParserConfig> action : actions) {
                action.accept(config);
            }

            Objects.requireNonNull(config.openAPI,
                    "Fusion Parser: Classpath is not provided.");

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

    private static final class Mapper<T> {
        private final Class<T> type;

        Mapper(Class<T> type) {
            this.type = type;
        }

        T map(URL url) {
            Objects.requireNonNull(url);

            ObjectMapper mapper = new ObjectMapper(new JsonFactory());

            try {
                return map(new File(url.toURI()), mapper);
            } catch (URISyntaxException e) {
                throw new ParserException(e);
            }
        }

        T map(File file) {
            String extension = FilenameUtils.getExtension(file.toString());

            ObjectMapper mapper;

            switch (extension) {
            case "yml":
            case "yaml":
                mapper = new ObjectMapper(new YAMLFactory());
                break;
            case "json":
                mapper = new ObjectMapper(new JsonFactory());
                break;
            default:
                throw new ParserException(String.format(
                        "The file format '.%s' is not supported", extension));
            }

            return map(file, mapper);
        }

        private T map(File file, ObjectMapper mapper) {
            try {
                return mapper.readValue(file, type);
            } catch (IOException e) {
                throw new ParserException("Failed to parse configuration file",
                        e);
            }
        }
    }
}
