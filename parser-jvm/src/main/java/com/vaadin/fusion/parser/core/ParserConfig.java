package com.vaadin.fusion.parser.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.swagger.v3.oas.models.servers.Server;

public final class ParserConfig {
    private final Application application = new Application();
    private final Plugins plugins = new Plugins();
    private String classPath;

    private ParserConfig() {
    }

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
        private static final String DEFAULT_APP_VERSION = "1.0.0-SNAPSHOT";
        private static final Server DEFAULT_SERVER = new Server()
                .description("Vaadin backend")
                .url("http://localhost:8080/connect");

        private String endpointAnnotation;
        private String name;
        private List<Server> servers;
        private String version;

        private Application() {
        }

        public String getEndpointAnnotation() {
            return endpointAnnotation != null ? endpointAnnotation
                    : DEFAULT_ENDPOINT_ANNOTATION;
        }

        public String getName() {
            return name != null ? name : DEFAULT_APP_NAME;
        }

        public List<Server> getServers() {
            return servers != null ? Collections.unmodifiableList(servers)
                    : Collections.singletonList(DEFAULT_SERVER);
        }

        public String getVersion() {
            return version != null ? version : DEFAULT_APP_VERSION;
        }
    }

    public static final class Plugins {
        private LinkedHashSet<String> disable;
        private LinkedHashSet<String> use;

        private Plugins() {
        }

        public Set<String> getDisable() {
            return disable != null ? Collections.unmodifiableSet(disable)
                    : Collections.emptySet();
        }

        public Set<String> getUse() {
            return use != null ? Collections.unmodifiableSet(use)
                    : Collections.emptySet();
        }
    }

    public static final class Factory {
        private final ParserConfig config;

        public Factory() {
            config = new ParserConfig();
            config.application.servers = new ArrayList<>();
            config.plugins.use = new LinkedHashSet<>();
            config.plugins.disable = new LinkedHashSet<>();
        }

        public Factory(File configFile) {
            String extension = FilenameUtils
                    .getExtension(configFile.toString());
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

        public Factory applicationName(String name) {
            return applicationName(name, true);
        }

        public Factory applicationName(String name, boolean override) {
            if (override || config.application.name == null) {
                config.application.name = name;
            }

            return this;
        }

        public Factory applicationVersion(String version) {
            return applicationVersion(version, true);
        }

        public Factory applicationVersion(String version, boolean override) {
            if (override || config.application.version == null) {
                config.application.version = version;
            }

            return this;
        }

        public Factory classPath(String classPath) {
            return classPath(classPath, true);
        }

        public Factory classPath(String classPath, boolean override) {
            if (override || config.classPath == null) {
                config.classPath = classPath;
            }

            return this;
        }

        public Factory disableDefaultPlugin(String plugin) {
            config.plugins.disable.add(plugin);
            return this;
        }

        public Factory disableDefaultPlugins(Collection<String> plugins) {
            return disableDefaultPlugins(plugins, true);
        }

        public Factory disableDefaultPlugins(Collection<String> plugins,
                boolean override) {
            if (override || config.plugins.disable == null) {
                config.plugins.disable = new LinkedHashSet<>(plugins);
            }
            return this;
        }

        public Factory disableDefaultPlugins(String... plugins) {
            return disableDefaultPlugins(Arrays.asList(plugins));
        }

        public Factory endpointAnnotation(String annotationQualifiedName) {
            return endpointAnnotation(annotationQualifiedName, true);
        }

        public Factory endpointAnnotation(String annotationQualifiedName,
                boolean override) {
            if (override || config.application.endpointAnnotation == null) {
                config.application.endpointAnnotation = annotationQualifiedName;
            }
            return this;
        }

        public ParserConfig finish() {
            return config;
        }

        public Factory useServer(Server server) {
            config.application.servers.add(server);
            return this;
        }

        public Factory useServers(Collection<Server> servers) {
            return useServers(servers, true);
        }

        public Factory useServers(Collection<Server> servers,
                boolean override) {
            if (override || config.application.servers == null) {
                config.application.servers = new ArrayList<>(servers);
            }
            return this;
        }

        public Factory useServers(Server... servers) {
            return useServers(Arrays.asList(servers));
        }

        public Factory usePlugin(String plugin) {
            config.plugins.use.add(plugin);
            return this;
        }

        public Factory usePlugins(Collection<String> plugins) {
            return usePlugins(plugins, true);
        }

        public Factory usePlugins(Collection<String> plugins,
                boolean override) {
            if (override || config.plugins.use == null) {
                config.plugins.use = new LinkedHashSet<>(plugins);
            }
            return this;
        }

        public Factory usePlugins(String... plugins) {
            return usePlugins(Arrays.asList(plugins));
        }
    }
}
