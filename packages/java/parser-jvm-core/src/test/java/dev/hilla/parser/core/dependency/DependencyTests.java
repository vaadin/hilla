package dev.hilla.parser.core.dependency;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.testutils.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DependencyTests {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private Set<String> defaultClassPathElements;
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
        defaultClassPathElements = Set.of(targetDir.toString());
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_PluginResolvesDependencies() {
        var parser = new Parser(new ParserConfig.Builder()
                .classPath(defaultClassPathElements)
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new DependencyPlugin()).finish());

        parser.execute();

        var expected = List.of(
                "dev.hilla.parser.core.dependency.DependencyEntityOne",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo",
                "dev.hilla.parser.core.dependency.PluginDependencyEntity");

        var actual = (List<String>) parser.getStorage().get().getPluginStorage()
                .get(DependencyPlugin.ENDPOINTS_DIRECT_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ReceiveDependenciesInPlugin() {
        var parser = new Parser(new ParserConfig.Builder()
                .classPath(defaultClassPathElements)
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new DependencyPlugin()).finish());

        parser.execute();

        var expected = List.of(
                "dev.hilla.parser.core.dependency.DependencyEntityOne",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo",
                "dev.hilla.parser.core.dependency.DependencyEntityThree");

        var actual = (List<String>) parser.getStorage().get().getPluginStorage()
                .get(DependencyPlugin.ENTITY_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependencyMembersCorrectly() {
        var parser = new Parser(new ParserConfig.Builder()
                .classPath(defaultClassPathElements)
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new DependencyPlugin()).finish());

        parser.execute();

        var expected = List.of("foo", "bar", "dependencyEntityThree", "foo2",
                "foo3");

        var actual = (List<String>) parser.getStorage().get().getPluginStorage()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
