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
package com.vaadin.hilla.parser.core.security.classlevel;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.ParserException;
import com.vaadin.hilla.parser.testutils.ResourceLoader;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

public class EndpointExposedAndSecurityAnnotationTest {

    private final List<String> classPath;
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private final List<Class<?>> endpoints = List.of(SomeEndpoint.class);

    {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void throws_when_parentEndpointClass_annotatedWithSecurityAnnotations() {
        var exception = assertThrows(ParserException.class,
                () -> new Parser().classPath(classPath)
                        .endpointAnnotations(List.of(Endpoint.class))
                        .endpointExposedAnnotations(
                                List.of(EndpointExposed.class))
                        .execute(endpoints));

        assertTrue(exception.getMessage().startsWith(
                "Class `com.vaadin.hilla.parser.core.security.classlevel.ParentEndpoint` is annotated with `com.vaadin.hilla.parser.testutils.annotations.EndpointExposed` and `jakarta.annotation.security.RolesAllowed` annotation."));

    }
}
