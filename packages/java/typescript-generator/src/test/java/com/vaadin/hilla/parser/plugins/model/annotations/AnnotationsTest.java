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
package com.vaadin.hilla.parser.plugins.model.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.plugins.model.Annotation;
import com.vaadin.hilla.parser.testutils.AbstractFullStackTest;

/**
 * The attributes of the annotations reach TypeScript through the OpenAPI
 * document, so they are asserted on the document itself: the TypeScript
 * snapshots only show the annotation names.
 */
public class AnnotationsTest extends AbstractFullStackTest {
    private static final String ENTITY = AnnotationsEndpoint.AnnotationTestEntity.class
            .getName();

    @Test
    public void should_GenerateTypescript() {
        assertTypescriptMatchesSnapshot(AnnotationsEndpoint.class);
    }

    @Test
    public void should_IncludeAnnotationAttributes() {
        var annotations = annotationsOf(parse(), "manyToManyWithFetchType");

        assertEquals(1, annotations.size());

        var annotation = annotations.get(0);
        assertEquals("jakarta.persistence.ManyToMany", annotation.getName());
        assertEquals(Map.of("fetch", "EAGER"), annotation.getAttributes());
    }

    @Test
    public void should_LeaveOutAttributesThatKeepTheirDefaults() {
        var annotations = annotationsOf(parse(), "manyToMany");

        assertEquals(1, annotations.size());
        assertNull(annotations.get(0).getAttributes());
    }

    private OpenAPI parse() {
        return generator(AnnotationsEndpoint.class).parse();
    }

    @SuppressWarnings("unchecked")
    private static List<Annotation> annotationsOf(OpenAPI openAPI,
            String propertyName) {
        var entity = openAPI.getComponents().getSchemas().get(ENTITY);
        assertTrue(entity != null, "no schema for " + ENTITY);

        var property = (Schema<?>) entity.getProperties().get(propertyName);
        assertTrue(property != null, "no property " + propertyName);

        return (List<Annotation>) property.getExtensions().get("x-annotations");
    }
}
