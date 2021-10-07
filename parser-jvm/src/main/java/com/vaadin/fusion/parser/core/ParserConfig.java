package com.vaadin.fusion.parser.core;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ParserConfig {
    private Application application = new Application();
    private String classPath;
    private final Plugins plugins = new Plugins();

    public ParserConfig() {
    }

    public static ParserConfig parse(final File config) {
        String extension = FilenameUtils.getExtension(config.toString());
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
            return mapper.readValue(config, ParserConfig.class);
        } catch (IOException e) {
            throw new ParserException("Failed to parse configuration file", e);
        }
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

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public static final class Application {
        private static final String DEFAULT_ENDPOINT_ANNOTATION = "com.vaadin.fusion.Endpoint";
        private static final String DEFAULT_APP_NAME = "Vaadin Application";

        private String name;
        private String endpointAnnotation;

        public String getEndpointAnnotation() {
            return endpointAnnotation != null ? endpointAnnotation
                    : DEFAULT_ENDPOINT_ANNOTATION;
        }

        public String getName() {
            return name != null ? name : DEFAULT_APP_NAME;
        }

        public void setEndpointAnnotation(final String endpointAnnotation) {
            this.endpointAnnotation = endpointAnnotation;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static final class Plugins {
        private List<String> disable;
        private List<String> use;

        public List<String> getDisable() {
            return disable != null ? disable : Collections.emptyList();
        }

        public List<String> getUse() {
            return use != null ? use : Collections.emptyList();
        }

        public void setDisable(List<String> disable) {
            this.disable = disable;
        }

        public void setUse(List<String> use) {
            this.use = use;
        }
    }
}
