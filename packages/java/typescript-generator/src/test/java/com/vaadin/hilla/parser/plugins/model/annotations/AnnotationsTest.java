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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.model.AnnotationModel;
import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.Generation;
import com.vaadin.hilla.parser.testutils.AbstractFullStackTest;

/**
 * The generated TypeScript is covered by the snapshot of this package. The
 * shape an attribute value takes is asserted on what the parser hands the
 * writers as well, since that is what the TypeScript is written from.
 */
public class AnnotationsTest extends AbstractFullStackTest {
    private static final String ENTITY = AnnotationsEndpoint.AnnotationTestEntity.class
            .getName();

    private Generation generation;

    @BeforeEach
    public void parseEndpoint() {
        generation = generator(AnnotationsEndpoint.class).parseGeneration();
    }

    @Test
    public void should_GenerateTypescript() {
        assertTypescriptMatchesSnapshot(AnnotationsEndpoint.class);
    }

    @Test
    public void should_IncludeAnnotationAttributes() {
        var annotations = annotationsOf("manyToManyWithFetchType");

        assertEquals(1, annotations.size());

        var annotation = annotations.get(0);
        assertEquals("jakarta.persistence.ManyToMany", annotation.name());
        assertEquals(Map.of("fetch", "EAGER"), annotation.attributes());
    }

    @Test
    public void should_ReferenceAClassAttributeByItsJvmType() {
        var attributes = annotationsOf("oneToManyWithTargetEntity").get(0)
                .attributes();

        assertEquals(
                Map.of("jvmType",
                        AnnotationsEndpoint.NestedEntity.class.getName()),
                attributes.get("targetEntity"));
    }

    @Test
    public void should_ConvertTheElementsOfAnArrayAttribute() {
        var attributes = annotationsOf("manyToManyWithCascade").get(0)
                .attributes();

        assertEquals(List.of("PERSIST", "MERGE"), attributes.get("cascade"));
    }

    @Test
    public void should_LeaveOutAttributesThatKeepTheirDefaults() {
        var annotations = annotationsOf("manyToMany");

        assertEquals(1, annotations.size());
        assertEquals(Map.of(), annotations.get(0).attributes());
    }

    private List<AnnotationModel> annotationsOf(String propertyName) {
        var entity = generation.entities().stream()
                .filter(candidate -> candidate.javaClass().equals(ENTITY))
                .map(EntityModel.Bean.class::cast).findFirst();
        assertTrue(entity.isPresent(), "no entity for " + ENTITY);

        var property = entity.get().properties().stream()
                .filter(candidate -> candidate.name().equals(propertyName))
                .findFirst();
        assertTrue(property.isPresent(), "no property " + propertyName);

        return property.get().type().annotations();
    }
}
