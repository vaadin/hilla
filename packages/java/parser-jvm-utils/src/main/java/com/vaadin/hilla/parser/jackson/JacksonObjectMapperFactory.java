package com.vaadin.hilla.parser.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

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
            return JsonMapper.builder().addModule(new ByteArrayModule())
                    .addModule(new Jdk8Module()).addModule(new JavaTimeModule())
                    .addModule(new ParameterNamesModule()).build();
        }
    }
}
