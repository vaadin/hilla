package com.vaadin.fusion.parser.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;

public final class ParserConfig {
    private final Application application = new Application();
    private final Plugins plugins = new Plugins();
    private String classPath;

    private ParserConfig() {}

    public Application getApplication() {
        return application;
    }

    public Optional<String> getClassPath() {
        return Optional.ofNullable(classPath);
    }

    public Plugins getPlugins() {
        return plugins;
    }

    public static final class Application {
        private static final String DEFAULT_ENDPOINT_ANNOTATION = "com.vaadin.fusion.Endpoint";
        private static final String DEFAULT_APP_NAME = "Vaadin Application";

        private String name;
        private String endpointAnnotation;

        private Application() {}

        public String getEndpointAnnotation() {
            return endpointAnnotation != null ? endpointAnnotation
                    : DEFAULT_ENDPOINT_ANNOTATION;
        }

        public String getName() {
            return name != null ? name : DEFAULT_APP_NAME;
        }
    }

    public static final class Plugins {
        private LinkedHashSet<String> disable;
        private LinkedHashSet<String> use;

        private Plugins() {}

        public Set<String> getDisable() {
            return disable != null ? Collections.unmodifiableSet(disable)
                    : Collections.emptySet();
        }

        public Set<String> getUse() {
            return use != null ? Collections.unmodifiableSet(use)
                    : Collections.emptySet();
        }
    }

    public static class Factory {
        private final ParserConfig config;

        public Factory() {
            config = new ParserConfig();
            config.plugins.use = new LinkedHashSet<>();
            config.plugins.disable = new LinkedHashSet<>();
        }

        public Factory(File configFile) {
            String extension = FilenameUtils.getExtension(configFile.toString());
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

            try {
                config = mapper.readValue(configFile, ParserConfig.class);
            } catch (IOException e) {
                throw new ParserException("Failed to parse configuration file",
                    e);
            }
        }

        public Factory classPath(String classPath) {
            config.classPath = classPath;
            return this;
        }

        public Factory usePlugin(String plugin) {
            config.plugins.use.add(plugin);
            return this;
        }

        public Factory usePlugins(Collection<String> plugins) {
            config.plugins.use = new LinkedHashSet<>(plugins);
            return this;
        }

        public Factory usePlugins(String... plugins) {
            return usePlugins(Arrays.asList(plugins));
        }

        public Factory disableDefaultPlugin(String plugin) {
            config.plugins.disable.add(plugin);
            return this;
        }

        public Factory disableDefaultPlugins(Collection<String> plugins) {
            config.plugins.disable = new LinkedHashSet<>(plugins);
            return this;
        }

        public Factory disableDefaultPlugins(String... plugins) {
            return disableDefaultPlugins(Arrays.asList(plugins));
        }

        public Factory applicationName(String name) {
            config.application.name = name;
            return this;
        }

        public Factory applicationEndpointAnnotation(
                String annotationQualifiedName) {
            config.application.endpointAnnotation = annotationQualifiedName;
            return this;
        }

        public ParserConfig finish() {
            return config;
        }
    }
}
