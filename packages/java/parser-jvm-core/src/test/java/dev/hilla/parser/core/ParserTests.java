package dev.hilla.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.basic.BasicPlugin;
import dev.hilla.parser.core.dependency.DependencyPlugin;
import dev.hilla.parser.testutils.ResourceLoader;

public class ParserTests {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private String basicPluginEndpointName;
    private Set<String> defaultClassPathElements;
    private String dependencyPluginEndpointName;
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        basicPluginEndpointName = dev.hilla.parser.core.basic.Endpoint.class
                .getName();
        dependencyPluginEndpointName = dev.hilla.parser.core.dependency.Endpoint.class
                .getName();

        targetDir = resourceLoader.findTargetDirPath();
        defaultClassPathElements = Set.of(targetDir.toString());
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_GetEndpointDirectDependencies() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(dependencyPluginEndpointName)
                        .addPlugin(new DependencyPlugin()).finish());

        parser.execute();

        var expected = List.of(
                "dev.hilla.parser.core.dependency.DependencyEntityOne",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.ENDPOINTS_DIRECT_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ReceiveDependenciesInPlugin() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(dependencyPluginEndpointName)
                        .addPlugin(new DependencyPlugin()).finish());

        parser.execute();

        var expected = List.of(
                "dev.hilla.parser.core.dependency.DependencyEntityOne",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo$InnerClass");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.ALL_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependencyMembersCorrectly() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(dependencyPluginEndpointName)
                        .addPlugin(new DependencyPlugin()).finish());

        parser.execute();

        var expected = List.of("foo", "bar", "foo", "circular", "getFoo",
                "setBar", "circular",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo$InnerClass");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_RunBasicPlugin() {
        var parser = new Parser(
                new ParserConfig.Builder().classPath(defaultClassPathElements)
                        .endpointAnnotation(basicPluginEndpointName)
                        .addPlugin(new BasicPlugin()).finish());

        parser.execute();

        var expected = List.of("foo", "bar", "getFoo", "baz", "getBar",
                "dev.hilla.parser.core.basic.BasicEndpoint$Bar",
                "dev.hilla.parser.core.basic.BasicEndpoint$Foo");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(BasicPlugin.STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
