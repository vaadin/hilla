package com.vaadin.hilla.parser.core.dependency;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.testutils.ResourceLoader;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DependencyTests {
    private static final List<String> classPath;
    private static final ResourceLoader resourceLoader = new ResourceLoader(
            DependencyTests.class);
    private static final List<Class<?>> endpoints = List
            .of(DependencyEndpoint.class);
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
        openApi = new Parser().classPath(classPath)
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new DependencyPlugin()).execute(endpoints);
    }

    @Test
    public void should_ResolveDependenciesCorrectly_When_ResolvingEntities() {
        var expected = List.of(
                "com.vaadin.hilla.parser.core.dependency.DependencyEntityOne",
                "com.vaadin.hilla.parser.core.dependency.DependencyEntityTwo",
                "com.vaadin.hilla.parser.core.dependency.DependencyEntityThree");

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
        var expected = Set.of("bar", "dependencyEntityThree", "foo", "foo2",
                "foo3");

        Collection<String> actual = (Collection<String>) openApi.getExtensions()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, new HashSet<>(actual));
    }
}
