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
package com.vaadin.hilla.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.parser.plugins.backbone.enumtype.EnumTypeEndpoint;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

/**
 * A generated enum holds what the mapper the endpoints use writes, and a form
 * sends one of those values back, so that mapper has to read each value as the
 * constant it was written from. Which values are generated, also with an
 * application's own mapper, is pinned by the snapshots of the enum cases.
 */
public class EnumValuesTest {
    @Test
    public void should_ReadBackEveryValueWithTheDefaultMapper() {
        assertReadBack(
                new FullStackGenerator(EnumValuesTest.class,
                        EnumTypeEndpoint.class),
                new JacksonObjectMapperFactory.Json().build());
    }

    private static void assertReadBack(FullStackGenerator generator,
            ObjectMapper mapper) {
        List<EntityModel.Enumeration> enums = generator.parseGeneration()
                .entities().stream()
                .filter(EntityModel.Enumeration.class::isInstance)
                .map(EntityModel.Enumeration.class::cast).toList();
        assertFalse(enums.isEmpty());

        for (var enumeration : enums) {
            for (var constant : enumeration.constants()) {
                var label = enumeration.javaClass() + "." + constant.name();

                assertEquals(constant.name(),
                        ((Enum<?>) mapper.readValue(
                                mapper.writeValueAsString(constant.value()),
                                load(enumeration.javaClass()))).name(),
                        label);
            }
        }
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
}
