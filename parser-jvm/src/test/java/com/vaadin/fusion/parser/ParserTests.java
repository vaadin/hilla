package com.vaadin.fusion.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.basic.BasicPlugin;
import com.vaadin.fusion.parser.dependency.DependencyPlugin;

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
        ParserConfig config = new ParserConfig()
                .classPath(targetDirPath.toString())
                .endpointAnnotation(Endpoint.class.getName())
                .plugins(Arrays.asList(BasicPlugin.class.getName()));

        Parser parser = new Parser(config);

        parser.execute();

        List<String> expected = Arrays.asList("foo", "bar", "getFoo", "baz",
                "getBar", "com.vaadin.fusion.parser.basic.BasicEndpoint$Bar",
                "com.vaadin.fusion.parser.basic.BasicEndpoint$Foo");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage().get(BasicPlugin.STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ReceiveDependenciesInPlugin() {
        ParserConfig config = new ParserConfig()
            .classPath(targetDirPath.toString())
            .endpointAnnotation(Endpoint.class.getName())
            .plugins(Arrays.asList(DependencyPlugin.class.getName()));

        Parser parser = new Parser(config);

        parser.execute();

        List<String> expected = Arrays.asList(
                "com.vaadin.fusion.parser.dependency.DependencyEntityOne",
                "com.vaadin.fusion.parser.dependency.DependencyEntityTwo",
                "com.vaadin.fusion.parser.dependency.DependencyEntityTwo$InnerClass");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage().get(DependencyPlugin.ALL_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_GetEndpointDirectDependencies() {
        ParserConfig config = new ParserConfig()
            .classPath(targetDirPath.toString())
            .endpointAnnotation(Endpoint.class.getName())
            .plugins(Arrays.asList(DependencyPlugin.class.getName()));

        Parser parser = new Parser(config);

        parser.execute();

        List<String> expected = Arrays.asList(
                "com.vaadin.fusion.parser.dependency.DependencyEntityOne",
                "com.vaadin.fusion.parser.dependency.DependencyEntityTwo");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage()
                .get(DependencyPlugin.ENDPOINTS_DIRECT_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependencyMembersCorrectly() {
        ParserConfig config = new ParserConfig()
            .classPath(targetDirPath.toString())
            .endpointAnnotation(Endpoint.class.getName())
            .plugins(Arrays.asList(DependencyPlugin.class.getName()));

        Parser parser = new Parser(config);

        parser.execute();

        List<String> expected = Arrays.asList("foo", "bar", "foo", "getFoo",
                "setBar", "getFoo",
                "com.vaadin.fusion.parser.dependency.DependencyEntityTwo$InnerClass");

        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
