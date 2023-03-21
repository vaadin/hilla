package dev.hilla.parser.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * A Jackson 2 module to allow for the registration of a custom serializer and
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
            extends JsonDeserializer<byte[]> {

        @Override
        public byte[] deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            return new ObjectMapper().readValue(
                    jp.getCodec().readTree(jp).toString(), byte[].class);
        }
    }

    private static class ByteArraySerializer extends JsonSerializer<byte[]> {

        @Override
        public void serialize(byte[] value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException {
            var arr = new int[value.length];

            for (int i = 0; i < value.length; i++) {
                arr[i] = value[i];
            }

            jgen.writeArray(arr, 0, arr.length);
        }
    }
}
