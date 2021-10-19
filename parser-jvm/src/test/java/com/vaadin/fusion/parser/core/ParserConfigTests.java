package com.vaadin.fusion.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.testutils.ResourceLoader;

import io.swagger.v3.oas.models.servers.Server;

public class ParserConfigTests {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
    }

    @Test
    public void should_LoadDefaultConfig_When_NoConfigOrOpenAPIFileSet() {
        ParserConfig config = new ParserConfig.Builder()
                .classPath(targetDir.toString()).finish();

        new ConfigComparator().compareTo(config);
    }

    @Test
    public void should_LoadJSONConfig() throws URISyntaxException {
        testConfigFile("parser-config.json");
    }

    @Test
    public void should_LoadYAMLConfig() throws URISyntaxException {
        testConfigFile("parser-config.yml");
    }

    @Test
    public void should_LoadJSONOpenAPITemplate() throws URISyntaxException {
        testOpenAPITemplate("openapi-template.json");
    }

    @Test
    public void should_LoadYAMLOpenAPITemplate() throws URISyntaxException {
        testOpenAPITemplate("openapi-template.yml");
    }

    @Test
    public void should_ThrowErrorIfNoClassPathSet() {
        assertThrows(NullPointerException.class,
                () -> new ParserConfig.Builder().finish(),
                "Fusion Parser Configuration: Classpath is not provided.");
    }

    private void testConfigFile(String configFileName)
            throws URISyntaxException {
        ParserConfig config = new ParserConfig.Builder()
                .classPath(targetDir.toString())
                .configFile(resourceLoader.find(configFileName)).finish();

        new ConfigComparator().endpointAnnotation("com.example.Endpoint")
                .pluginsDisable(Collections.singleton("backbone"))
                .pluginsUse(new LinkedHashSet<>(
                        Arrays.asList("com.vaadin.fusion.parser.BasicPlugin",
                                "com.vaadin.fusion.parser.DependencyPlugin")))
                .compareTo(config);
    }

    private void testOpenAPITemplate(String openAPITemplateName)
            throws URISyntaxException {
        ParserConfig config = new ParserConfig.Builder()
                .classPath(targetDir.toString())
                .openAPITemplate(resourceLoader.find(openAPITemplateName))
                .finish();

        new ConfigComparator().applicationName("My Cool Application")
                .applicationVersion("2.5.9-SNAPSHOT")
                .servers(Collections.singletonList(
                        new Server().url("https://app.cool.my/connect")
                                .description("My Cool Backend")))
                .compareTo(config);
    }

    private class ConfigComparator {
        private String applicationName = "Vaadin Application";
        private String applicationVersion = "1.0.0";
        private String classPath = targetDir.toString();
        private String endpointAnnotation = "com.vaadin.fusion.Endpoint";
        private String openAPIVersion = "3.0.1";
        private Set<String> pluginsDisable = Collections.emptySet();
        private Set<String> pluginsUse = Collections.emptySet();
        private List<Server> servers = Collections
                .singletonList(new Server().url("http://localhost:8080/connect")
                        .description("Vaadin Backend"));

        public ConfigComparator applicationName(String value) {
            applicationName = value;
            return this;
        }

        public ConfigComparator applicationVersion(String value) {
            applicationVersion = value;
            return this;
        }

        public ConfigComparator classPath(String value) {
            classPath = value;
            return this;
        }

        public void compareTo(ParserConfig config) {
            assertEquals(applicationName,
                    config.getOpenAPI().getInfo().getTitle());
            assertEquals(applicationVersion,
                    config.getOpenAPI().getInfo().getVersion());
            assertEquals(classPath, config.getClassPath());
            assertEquals(endpointAnnotation,
                    config.getApplication().getEndpointAnnotation());
            assertEquals(openAPIVersion, config.getOpenAPI().getOpenapi());
            assertEquals(pluginsDisable, config.getPlugins().getDisable());
            assertEquals(pluginsUse, config.getPlugins().getUse());
            assertEquals(servers, config.getOpenAPI().getServers());
        }

        public ConfigComparator endpointAnnotation(String value) {
            endpointAnnotation = value;
            return this;
        }

        public ConfigComparator openAPIVersion(String value) {
            openAPIVersion = value;
            return this;
        }

        public ConfigComparator pluginsDisable(Set<String> value) {
            pluginsDisable = value;
            return this;
        }

        public ConfigComparator pluginsUse(Set<String> value) {
            pluginsUse = value;
            return this;
        }

        public ConfigComparator servers(List<Server> value) {
            servers = value;
            return this;
        }

    }
}
