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
package com.vaadin.hilla.parser.plugins.subtypes.typeids;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import com.vaadin.hilla.parser.testutils.AbstractFullStackTest;

/**
 * Verifies the type ids that the generator gives to the subtypes of a
 * polymorphic type. Which value ends up in the generated TypeScript is decided
 * by the parser, so these hierarchies are verified in the parsed document,
 * except for the one that must get no property at all: how a discriminator is
 * rendered is covered by the snapshots of the surrounding package.
 */
public class TypeIdsTest extends AbstractFullStackTest {
    /**
     * The instances whose type id is checked: one per class mentioned in a
     * {@code @JsonSubTypes} annotation of the package.
     */
    private static final List<Object> SUBTYPES = List.of(new NameBase.Listed(),
            new NameBase.Named(), new NameBase.Nested(),
            new SimpleBase.Nested(), new SimpleBase.FromInterface(),
            new SimpleBase.FromSuperclass(), new MinimalBase.Subtype(),
            new MinimalBase.OtherSubtype());

    /**
     * The subtypes that get no discriminator at all, because Jackson builds
     * their type ids from the name of the base class, which is not known here:
     * a generated property would hold values that the server never sends.
     */
    private static final List<String> WITHOUT_DISCRIMINATOR = List
            .of("MinimalBase$Subtype", "MinimalBase$OtherSubtype");

    /**
     * A discriminator is only useful if it holds the value that the server
     * actually sends. Every generated discriminator is therefore compared with
     * the JSON that the Jackson mapper of an application produces for an
     * instance of the type: a value that the server never sends leaves the
     * TypeScript type unusable for the value it describes.
     *
     * <p>
     * The value is written the way an endpoint writes its return value, from
     * the object alone rather than from a declared type. It matters: with a
     * declared type, Jackson resolves the id of a subtype from the
     * {@code @JsonSubTypes} annotation of that type alone, and a name a subtype
     * inherits from a supertype is not seen.
     */
    @Test
    public void should_DeclareTheTypeIdsThatJacksonSends() {
        var openApi = generator(TypeIdsEndpoint.class).parse();
        var mapper = new JsonMapper();
        var generated = new LinkedHashMap<String, String>();
        var serialized = new LinkedHashMap<String, String>();

        SUBTYPES.forEach(instance -> {
            var cls = instance.getClass();

            discriminatorProperty(openApi, cls).ifPresent(property -> {
                generated.put(nameOf(cls),
                        property + "="
                                + discriminatorValue(openApi, cls, property)
                                        .orElse(null));

                var json = mapper.readTree(mapper.writeValueAsString(instance));
                serialized.put(nameOf(cls),
                        property + "=" + Optional.ofNullable(json.get(property))
                                .map(node -> node.asString()).orElse(null));
            });
        });

        assertEquals(WITHOUT_DISCRIMINATOR, SUBTYPES.stream()
                .map(Object::getClass)
                .filter(cls -> discriminatorProperty(openApi, cls).isEmpty())
                .map(TypeIdsTest::nameOf).toList(),
                "Another set of subtypes than the expected one is left without"
                        + " a discriminator");
        assertEquals(serialized, generated);
    }

    /**
     * A hierarchy whose type ids are unknown gets no property at all: an
     * identifying property that the server never sends would be worse than none
     * at all, as TypeScript would demand it from every value.
     */
    @Test
    public void should_NotDeclareAnIdentifyingPropertyForUnknownTypeIds() {
        var sources = generator(TypeIdsEndpoint.class).generate();
        var path = MinimalBase.Subtype.class.getName().replace('.', '/')
                .replace('$', '/') + ".ts";
        var source = sources.get(path);

        assertNotNull(source, () -> "The generator produced no " + path
                + ", only " + sources.keySet());
        assertFalse(source.contains("@c"),
                () -> "The generated " + path + " declares the property that"
                        + " Jackson uses for a class based type id:\n"
                        + source);
    }

    /**
     * The name of the discriminator property of the given class, which the
     * parser publishes in the union schema of the type that declares the
     * subtypes, or an empty optional if no discriminator is generated for it.
     */
    private static Optional<String> discriminatorProperty(OpenAPI openApi,
            Class<?> cls) {
        var ref = "#/components/schemas/" + cls.getName();

        for (Schema<?> schema : schemasOf(openApi).values()) {
            var discriminator = schema.getDiscriminator();

            if (discriminator == null || schema.getOneOf() == null) {
                continue;
            }

            for (Object item : schema.getOneOf()) {
                if (ref.equals(((Schema<?>) item).get$ref())) {
                    return Optional.of(discriminator.getPropertyName());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * The value that the generated discriminator property of the given class
     * holds: the parser stores the own value of the type first, followed by the
     * values of the subtypes below it.
     */
    private static Optional<String> discriminatorValue(OpenAPI openApi,
            Class<?> cls, String property) {
        Schema<?> schema = schemasOf(openApi).get(cls.getName());
        List<?> ownSchemas = schema.getAnyOf() != null ? schema.getAnyOf()
                : List.of(schema);

        for (Object own : ownSchemas) {
            var properties = ((Schema<?>) own).getProperties();

            if (properties == null) {
                continue;
            }

            var item = (Schema<?>) properties.get(property);

            if (item != null && item.getEnum() != null
                    && !item.getEnum().isEmpty()) {
                return Optional.of(String.valueOf(item.getEnum().get(0)));
            }
        }

        return Optional.empty();
    }

    private static Map<String, Schema> schemasOf(OpenAPI openApi) {
        return openApi.getComponents().getSchemas();
    }

    /**
     * The name of the given class without its package, which tells the nested
     * subtypes of two hierarchies apart, unlike the simple class name.
     */
    private static String nameOf(Class<?> cls) {
        var name = cls.getName();

        return name.substring(name.lastIndexOf('.') + 1);
    }
}
