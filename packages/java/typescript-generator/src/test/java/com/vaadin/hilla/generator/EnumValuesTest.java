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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.config.CustomConfigEndpoint;
import com.vaadin.hilla.parser.plugins.backbone.config.CustomFactory;
import com.vaadin.hilla.parser.plugins.backbone.enumtype.EnumTypeEndpoint;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

/**
 * A generated enum holds what the mapper the endpoints use writes, and a form
 * sends one of those values back, so that mapper has to read each value as the
 * constant it was written from. Both are asked of the mapper itself, the
 * default one and an application's own, rather than of fixed snapshots.
 */
public class EnumValuesTest {
    @Test
    public void should_ReadBackEveryValueWithTheDefaultMapper() {
        assertReadBack(
                new FullStackGenerator(EnumValuesTest.class,
                        EnumTypeEndpoint.class),
                new JacksonObjectMapperFactory.Json().build());
    }

    @Test
    public void should_ReadBackEveryValueWithTheMapperOfTheApplication() {
        var configuration = new BackbonePluginConfiguration();
        configuration
                .setObjectMapperFactoryClassName(CustomFactory.class.getName());
        var plugin = new BackbonePlugin();
        plugin.setConfiguration(configuration);

        assertReadBack(
                new FullStackGenerator(EnumValuesTest.class,
                        CustomConfigEndpoint.class).withPlugin(plugin),
                new CustomFactory().build());
    }

    private static void assertReadBack(FullStackGenerator generator,
            ObjectMapper mapper) {
        List<EntityModel.Enumeration> enums = generator.parseGeneration()
                .entities().stream()
                .filter(EntityModel.Enumeration.class::isInstance)
                .map(EntityModel.Enumeration.class::cast).toList();
        assertFalse(enums.isEmpty());

        for (var enumeration : enums) {
            var constants = load(enumeration.javaClass()).getEnumConstants();

            for (var constant : enumeration.constants()) {
                var written = Arrays.stream(constants)
                        .filter(c -> ((Enum<?>) c).name()
                                .equals(constant.name()))
                        .findFirst()
                        .<JsonNode> map(mapper::valueToTree).orElseThrow();
                var label = enumeration.javaClass() + "." + constant.name();

                // What the server sends, where it sends a string: a constant
                // written as a number keeps its name, a known limitation
                if (written.isString()) {
                    assertEquals(written.stringValue(), constant.value(),
                            label);
                }

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
