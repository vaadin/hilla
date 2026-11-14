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
package com.vaadin.hilla.parser.jackson;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * A factory to build a custom ObjectMapper for serializing and deserializing
 * data exchange between server and client in runtime; also, it is used by the
 * JVM parser to extract properties from the data beans.
 * <p>
 * The values of the generated TypeScript enums come from this mapper too: each
 * constant the mapper writes as a string is generated as that string. A
 * constant written as anything else, such as a number with
 * {@code @JsonFormat(shape = NUMBER)}, keeps its name, so the generated enum
 * does not match what the server sends for it. A setting that changes how enums
 * are written, such as {@code EnumFeature.WRITE_ENUMS_TO_LOWERCASE}, therefore
 * changes the generated enum values, and client code comparing against the
 * previous values has to follow. The mapper should then also read those values
 * back, for example with {@code MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS},
 * since a form sends them to the server as they are.
 * <p>
 * The generator only uses a factory it is given by class name, through
 * {@code BackbonePluginConfiguration.setObjectMapperFactoryClassName}. A
 * factory supplied only as the {@code endpointMapperFactory} bean changes what
 * the server sends but not what is generated, so the two can then disagree.
 * <p>
 * For example, if you want to have the behavior of the old engine, you should
 * create the following factory:
 *
 * <pre>
 * public class OldEngineObjectMapper extends JacksonObjectMapperFactory.Json {
 *     &#64;Override
 *     public ObjectMapper build() {
 *         return super.build().setVisibility(PropertyAccessor.ALL,
 *                 JsonAutoDetect.Visibility.ANY);
 *     }
 * }
 * </pre>
 */
public interface JacksonObjectMapperFactory {
    ObjectMapper build();

    class Json implements JacksonObjectMapperFactory {
        @Override
        public ObjectMapper build() {
            // In Jackson 3, Jdk8Module, JavaTimeModule, and
            // ParameterNamesModule are built into jackson-databind
            return JsonMapper.builder().addModule(new ByteArrayModule())
                    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                    // Configure Jackson 3 to be compatible with Jackson 2 type
                    // conversion behavior
                    .configure(
                            DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                            false)
                    .enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT).build();
        }
    }
}
