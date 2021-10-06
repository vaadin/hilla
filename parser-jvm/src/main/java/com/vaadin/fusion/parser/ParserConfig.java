package com.vaadin.fusion.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ParserConfig {
    private String classPath;
    private String endpointAnnotation;
    private List<String> plugins;

    public ParserConfig() {}

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

    public ParserConfig classPath(String value) {
        classPath = value;
        return this;
    }

    public ParserConfig endpointAnnotation(String value) {
        endpointAnnotation = value;
        return this;
    }

    public Optional<String> getClassPath() {
        return Optional.ofNullable(classPath);
    }

    public ParserConfig plugins(List<String> value) {
        plugins = value;
        return this;
    }

    public Optional<String> getEndpointAnnotation() {
        return Optional.ofNullable(endpointAnnotation);
    }

    public Optional<List<String>> getPlugins() {
        return Optional.ofNullable(plugins);
    }
}
