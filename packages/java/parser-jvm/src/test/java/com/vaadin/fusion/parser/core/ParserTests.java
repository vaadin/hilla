package com.vaadin.fusion.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.core.basic.BasicPlugin;
import com.vaadin.fusion.parser.core.dependency.DependencyPlugin;
import com.vaadin.fusion.parser.testutils.Endpoint;

public class ParserTests {
    private Path targetDirPath;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDirPath = Paths.get(
                Objects.requireNonNull(getClass().getResource("/")).toURI())
                .getParent();
    }

    @Test
    public void should_RunBasicPlugin() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
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
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
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
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
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
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
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
