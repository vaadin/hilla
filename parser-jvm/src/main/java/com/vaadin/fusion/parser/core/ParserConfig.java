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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

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

    public Application getApplication() {
        return application;
    }

    public Optional<String> getClassPath() {
        return Optional.ofNullable(classPath);
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

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

        public Builder adjustOpenAPI(Consumer<OpenAPI> action) {
            actions.add(config -> {
                action.accept(config.openAPI);
            });
            return this;
        }

        public Builder classPath(String classPath) {
            return classPath(classPath, true);
        }

        public Builder classPath(String classPath, boolean override) {
            actions.add(config -> {
                if (override || config.classPath == null) {
                    config.classPath = classPath;
                }
            });

            return this;
        }

        public Builder configFile(File file) {
            configFile = file;
            return this;
        }

        public Builder disableDefaultPlugin(String plugin) {
            actions.add(config -> config.plugins.disable.add(plugin));
            return this;
        }

        public Builder disableDefaultPlugins(Collection<String> plugins) {
            return disableDefaultPlugins(plugins, true);
        }

        public Builder disableDefaultPlugins(Collection<String> plugins,
                boolean override) {
            actions.add(config -> {
                if (override || config.plugins.disable == null) {
                    config.plugins.disable = new LinkedHashSet<>(plugins);
                }
            });
            return this;
        }

        public Builder disableDefaultPlugins(String... plugins) {
            return disableDefaultPlugins(Arrays.asList(plugins));
        }

        public Builder endpointAnnotation(String annotationQualifiedName) {
            return endpointAnnotation(annotationQualifiedName, true);
        }

        public Builder endpointAnnotation(String annotationQualifiedName,
                boolean override) {
            actions.add(config -> {
                if (override || config.application.endpointAnnotation == null) {
                    config.application.endpointAnnotation = annotationQualifiedName;
                }
            });
            return this;
        }

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

            return config;
        }

        public Builder openAPITemplate(File file) {
            openAPITemplate = file;
            return this;
        }

        public Builder usePlugin(String plugin) {
            actions.add(config -> config.plugins.use.add(plugin));
            return this;
        }

        public Builder usePlugins(Collection<String> plugins) {
            return usePlugins(plugins, true);
        }

        public Builder usePlugins(Collection<String> plugins,
                boolean override) {
            actions.add(config -> {
                if (override || config.plugins.use == null) {
                    config.plugins.use = new LinkedHashSet<>(plugins);
                }
            });
            return this;
        }

        public Builder usePlugins(String... plugins) {
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

        public Set<String> getDisable() {
            return Collections.unmodifiableSet(disable);
        }

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
