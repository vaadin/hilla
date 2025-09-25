package com.vaadin.hilla.parser.jackson;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * A factory to build a custom ObjectMapper for serializing and deserializing
 * data exchange between server and client in runtime; also, it is used by the
 * JVM parser to extract properties from the data beans.
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
            // In Jackson 3.x, Jdk8Module, JavaTimeModule, and
            // ParameterNamesModule
            // are built into jackson-databind and no longer separate modules
            return JsonMapper.builder().addModule(new ByteArrayModule())
                    .build();
        }
    }
}
