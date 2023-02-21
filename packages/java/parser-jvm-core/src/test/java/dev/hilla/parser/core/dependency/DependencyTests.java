package dev.hilla.parser.core.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.testutils.ResourceLoader;

import io.swagger.v3.oas.models.OpenAPI;

public class DependencyTests {
    private static final List<String> classPath;
    private static final ResourceLoader resourceLoader = new ResourceLoader(
            DependencyTests.class);
    private static OpenAPI openApi;

    static {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void setUp() {
        openApi = new Parser()
                .classLoader(DependencyTests.class.getClassLoader())
                .classPath(classPath)
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new DependencyPlugin()).execute();
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ResolvingEntities() {
        var expected = List.of(
                "dev.hilla.parser.core.dependency.DependencyEntityOne",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo",
                "dev.hilla.parser.core.dependency.DependencyEntityThree");

        var actual = openApi.getExtensions()
                .get(DependencyPlugin.ENTITY_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ResolvingMethods() {
        var expected = List.of("getEntityOne", "getEntityTwo");

        var actual = openApi.getExtensions()
                .get(DependencyPlugin.ENDPOINTS_DIRECT_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependencyMembersCorrectly() {
        var expected = List.of("bar", "dependencyEntityThree", "foo", "foo2",
                "foo3");

        var actual = openApi.getExtensions()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
