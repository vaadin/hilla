/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.core.dependency;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;
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
