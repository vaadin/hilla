package com.vaadin.fusion.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.core.basic.BasicPlugin;
import com.vaadin.fusion.parser.core.dependency.DependencyPlugin;

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
        Parser parser = new Parser(
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
                        .usePlugin(BasicPlugin.class.getName()).finish());

        parser.execute();

        List<String> expected = Arrays.asList("foo", "bar", "getFoo", "baz",
                "getBar",
                "com.vaadin.fusion.parser.core.basic.BasicEndpoint$Bar",
                "com.vaadin.fusion.parser.core.basic.BasicEndpoint$Foo");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage().get(BasicPlugin.STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ReceiveDependenciesInPlugin() {
        Parser parser = new Parser(
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
                        .usePlugin(DependencyPlugin.class.getName()).finish());

        parser.execute();

        List<String> expected = Arrays.asList(
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityOne",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo$InnerClass");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage().get(DependencyPlugin.ALL_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_GetEndpointDirectDependencies() {
        Parser parser = new Parser(
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
                        .usePlugin(DependencyPlugin.class.getName()).finish());

        parser.execute();

        List<String> expected = Arrays.asList(
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityOne",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage()
                .get(DependencyPlugin.ENDPOINTS_DIRECT_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependencyMembersCorrectly() {
        Parser parser = new Parser(
                new ParserConfig.Builder().classPath(targetDirPath.toString())
                        .endpointAnnotation(Endpoint.class.getName())
                        .usePlugin(DependencyPlugin.class.getName()).finish());

        parser.execute();

        List<String> expected = Arrays.asList("foo", "bar", "foo", "circular",
                "getFoo", "setBar", "circular",
                "com.vaadin.fusion.parser.core.dependency.DependencyEntityTwo$InnerClass");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
