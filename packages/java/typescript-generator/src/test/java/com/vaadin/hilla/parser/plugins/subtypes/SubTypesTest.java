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
package com.vaadin.hilla.parser.plugins.subtypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import com.vaadin.hilla.parser.testutils.AbstractFullStackTest;

public class SubTypesTest extends AbstractFullStackTest {
    /**
     * The instances whose type id is checked: one per class mentioned in a
     * {@code @JsonSubTypes} annotation of the package.
     */
    private static final List<Object> SUBTYPES = List.of(new AddEvent(),
            new UpdateEvent(), new DeleteEvent(), new MoveEvent(),
            new Notification(), new EmailNotification(),
            new HtmlEmailNotification(), new MultipartSmsNotification(),
            new BaseEvent.NestedEvent(), new Circle(1), new Square(1),
            new BatchJob(), new CronJob(), new NightlyJob(),
            new Job.InlineJob(), new TextPayload(), new BinaryPayload());

    /**
     * The subtypes that get no discriminator at all, because Jackson builds
     * their type ids from the name of the base class, which is not known here:
     * the {@code Payload} hierarchy uses {@code Id.MINIMAL_CLASS}, so a
     * generated property would hold values that the server never sends.
     */
    private static final List<String> WITHOUT_DISCRIMINATOR = List
            .of("TextPayload", "BinaryPayload");

    @Test
    public void should_GenerateTheExpectedTypeScript() {
        assertTypescriptMatchesSnapshot(SubTypesEndpoint.class);
    }

    /**
     * A discriminator is only useful if it holds the value that the server
     * actually sends, which the snapshots cannot tell. Every generated
     * discriminator is therefore compared with the JSON that Jackson produces
     * for an instance of the type: a value that the server never sends leaves
     * the TypeScript type unusable for the value it describes.
     */
    @Test
    public void should_DeclareTheTypeIdsThatJacksonSends() {
        var openApi = generator(SubTypesEndpoint.class).parse();
        var mapper = new JsonMapper();
        var generated = new LinkedHashMap<String, String>();
        var serialized = new LinkedHashMap<String, String>();

        SUBTYPES.forEach(instance -> {
            var cls = instance.getClass();

            discriminatorProperty(openApi, cls).ifPresent(property -> {
                generated.put(cls.getSimpleName(),
                        property + "="
                                + discriminatorValue(openApi, cls, property)
                                        .orElse(null));

                var json = mapper.readTree(mapper.writeValueAsString(instance));
                serialized.put(cls.getSimpleName(),
                        property + "=" + Optional.ofNullable(json.get(property))
                                .map(node -> node.asString()).orElse(null));
            });
        });

        assertEquals(WITHOUT_DISCRIMINATOR, SUBTYPES.stream()
                .map(Object::getClass)
                .filter(cls -> discriminatorProperty(openApi, cls).isEmpty())
                .map(Class::getSimpleName).toList(),
                "Another set of subtypes than the expected one is left without"
                        + " a discriminator");
        assertEquals(serialized, generated);
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
}
