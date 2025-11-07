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

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * A Jackson module to allow for the registration of a custom serializer and
 * deserializer for byte[]. By default, Jackson converts byte arrays to base64
 * string. In contrast, Hilla promises to send and receive byte[] as an array of
 * numbers on the client-side. The custom serializer and deserializer of this
 * class ensures that Java byte[] are indeed treated as array of numbers.
 */
public class ByteArrayModule extends SimpleModule {

    public ByteArrayModule() {
        super();
        addSerializer(byte[].class, new ByteArraySerializer());
        addDeserializer(byte[].class, new ByteArrayDeSerializer());
    }

    private static class ByteArrayDeSerializer
            extends ValueDeserializer<byte[]> {

        @Override
        public byte[] deserialize(JsonParser jp, DeserializationContext ctxt) {
            return new ObjectMapper().readValue(jp.readValueAsTree().toString(),
                    byte[].class);
        }
    }

    private static class ByteArraySerializer extends ValueSerializer<byte[]> {

        @Override
        public void serialize(byte[] value, JsonGenerator jgen,
                SerializationContext provider) {
            var arr = new int[value.length];

            for (int i = 0; i < value.length; i++) {
                arr[i] = value[i];
            }

            jgen.writeArray(arr, 0, arr.length);
        }
    }
}
