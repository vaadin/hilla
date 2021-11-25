package com.vaadin.fusion.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.core.basic.BasicPlugin;
import com.vaadin.fusion.parser.core.dependency.DependencyPlugin;
import com.vaadin.fusion.parser.testutils.ResourceLoader;

public class ParserTests {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private String basicPluginEndpointName;
    private String dependencyPluginEndpointName;
    private Path targetDir;
    private Set<String> defaultClassPathElements;

    @BeforeEach
    public void setup() throws URISyntaxException {
        basicPluginEndpointName = com.vaadin.fusion.parser.core.basic.Endpoint.class
                .getName();
        dependencyPluginEndpointName = com.vaadin.fusion.parser.core.dependency.Endpoint.class
                .getName();

        targetDir = resourceLoader.findTargetDirPath();
        defaultClassPathElements = Set.of(targetDir.toString());
    }

    @Test
    public void should_RunBasicPlugin() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(basicPluginEndpointName)
                        .addPlugin(BasicPlugin.class.getName()).finish());

        parser.execute();

        var expected = List.of("foo", "bar", "getFoo", "baz", "getBar",
                "com.vaadin.fusion.parser.core.basic.BasicEndpoint$Bar",
                "com.vaadin.fusion.parser.core.basic.BasicEndpoint$Foo");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(BasicPlugin.STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ReceiveDependenciesInPlugin() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(dependencyPluginEndpointName)
                        .addPlugin(DependencyPlugin.class.getName()).finish());

        parser.execute();

        var expected = List.of(
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityOne",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo$InnerClass");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.ALL_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_GetEndpointDirectDependencies() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(dependencyPluginEndpointName)
                        .addPlugin(DependencyPlugin.class.getName()).finish());

        parser.execute();

        var expected = List.of(
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityOne",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.ENDPOINTS_DIRECT_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependencyMembersCorrectly() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(dependencyPluginEndpointName)
                        .addPlugin(DependencyPlugin.class.getName()).finish());

        parser.execute();

        var expected = List.of("foo", "bar", "foo", "circular", "getFoo",
                "setBar", "circular",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo$InnerClass");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
