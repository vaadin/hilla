/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.hilla.endpointransfermapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines mappings for certain endpoint types to corresponding transfer types.
 *
 * This allows using beans / classes in endpoints which cannot be serialized by
 * the standard bean serialization.
 * <p>
 * Each endpoint parameter value coming from the client is passed through a
 * registered mapper for that endpoint type before the values are passed to the
 * endpoint methods.
 * <p>
 * Each endpoint return value is passed through a registered mapper before the
 * value is serialized by the normal serialization mechanism and then sent to
 * the client.
 * <p>
 * The endpoint TypeScript is generated based on the mapped types.
 * <p>
 * For example, a method like
 * <p>
 * <code>
 * public Page&lt;Person&gt; list(Pageable pageable) {
 * }
 * </code>
 * <p>
 * generates a TypeScript and converts data as if the method was defined as
 * <p>
 * <code>
 * public List&lt;Person&gt; list(com.vaadin.hilla.mappedtypes.Pageable pageable) {
 * }
 * </code>
 *
 */
public class EndpointTransferMapper {

    /**
     * A mapper for endpoint values that is capable of converting between the
     * given endpoint and transfer type.
     *
     * @param <ENDPOINTTYPE>
     *            The type used in endpoints
     * @param <TRANSFERTYPE>
     *            the type used in communication with the client
     */
    public interface Mapper<ENDPOINTTYPE, TRANSFERTYPE> {
        /**
         * Returns the type used in the endpoint.
         *
         *
         * @return the endpoint type
         */
        Class<? extends ENDPOINTTYPE> getEndpointType();

        /**
         * Returns the type used when transfering data to/from the client.
         *
         * @return the transfer type
         */
        Class<? extends TRANSFERTYPE> getTransferType();

        /**
         * Converts the given endpoint value to the transfer type.
         *
         * @param endpointType
         *            the value used in the endpoint
         * @return the value used in communication with the client
         */
        TRANSFERTYPE toTransferType(ENDPOINTTYPE endpointType);

        /**
         * Converts the given transfer value to the endpoint type.
         *
         * @param transferType
         *            the value used in communication with the client
         * @return the value used in the endpoint
         */
        ENDPOINTTYPE toEndpointType(TRANSFERTYPE transferType);

    }

    private Map<Class<?>, Class<?>> endpointToTransfer = new HashMap<>();

    private Map<Class<?>, Mapper<?, ?>> mappers = new HashMap<>();

    private final SimpleModule jacksonModule = new SimpleModule();

    /**
     * Creates a new instance.
     */
    public EndpointTransferMapper() {
        registerMapper(new PageableMapper());
        registerMapper(new UUIDMapper());
        registerMapper(new PageMapper());
    }

    /**
     * Register a mapper that maps all objects of the given endpoint type to the
     * given transfer type.
     *
     * @param mapper
     *            the mapper to register
     */
    private <ENDPOINTTYPE, TRANSFERTYPE> void registerMapper(
            Mapper<ENDPOINTTYPE, TRANSFERTYPE> mapper) {
        var endpointType = (Class<ENDPOINTTYPE>) mapper.getEndpointType();
        var transferType = mapper.getTransferType();
        endpointToTransfer.put(endpointType, mapper.getTransferType());
        mappers.put(endpointType, mapper);

        var serializer = new StdDelegatingSerializer(
                new StdConverter<ENDPOINTTYPE, TRANSFERTYPE>() {
                    @Override
                    public TRANSFERTYPE convert(ENDPOINTTYPE value) {
                        return mapper.toTransferType(value);
                    }

                    @Override
                    public JavaType getInputType(TypeFactory typeFactory) {
                        return typeFactory.constructType(endpointType);
                    }

                    @Override
                    public JavaType getOutputType(TypeFactory typeFactory) {
                        return typeFactory.constructType(transferType);
                    }
                });
        jacksonModule.addSerializer(endpointType, serializer);

        var deserializer = new StdDelegatingDeserializer<>(
                new StdConverter<TRANSFERTYPE, ENDPOINTTYPE>() {
                    @Override
                    public ENDPOINTTYPE convert(TRANSFERTYPE value) {
                        return mapper.toEndpointType(value);
                    }

                    @Override
                    public JavaType getInputType(TypeFactory typeFactory) {
                        return typeFactory.constructType(transferType);
                    }

                    @Override
                    public JavaType getOutputType(TypeFactory typeFactory) {
                        return typeFactory.constructType(endpointType);
                    }
                });

        jacksonModule.addDeserializer(endpointType, deserializer);
    }

    /**
     * Gets the Jackson 2 module configured with the mapping serializers and
     * deserializers.
     *
     * <p>
     * For internal use only. May be changed or removed in a future release.
     *
     * @return Jackson 2 module.
     */
    public Module getJacksonModule() {
        return jacksonModule;
    }

    /**
     * Gets the transfer type for the given endpoint type.
     * <p>
     * NOTE that this is intended for checking the type of a value being sent at
     * runtime and thus checks also the super types / interfaces of the given
     * type.
     * <p>
     * The returned transfer type is the same as the endpoint type if no
     * conversion is needed.
     *
     * @param endpointType
     *            the endpoint type
     * @return the transfer type or null if no mapping exists
     */
    public Class<?> getTransferType(Class<?> endpointType) {
        for (Entry<Class<?>, Class<?>> entry : endpointToTransfer.entrySet()) {
            if (entry.getKey().isAssignableFrom(endpointType)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the transfer type for the given endpoint type.
     * <p>
     * NOTE that this is intended to be used when generating TypeScript and it
     * DOES NOT check super types / interfaces of the given type.
     * <p>
     * The returned transfer type is the same as the endpoint type if no
     * conversion is needed.
     *
     * @param endpointType
     *            the endpoint type
     * @return the transfer type or null if no mapping exists
     */
    public String getTransferType(String endpointType) {
        for (Entry<Class<?>, Class<?>> entry : endpointToTransfer.entrySet()) {
            if (entry.getKey().getName().equals(endpointType)) {
                return entry.getValue().getName();
            }
        }
        return null;
    }

    /**
     * Gets the mapper for the given endpoint type.
     * <p>
     * NOTE that this is intended for runtime and thus checks also the super
     * types / interfaces of the given type.
     *
     * @param endpointType
     *            the endpoint type
     * @param <T>
     *            the endpoint type
     * @return the transfer type or null if no mapper exists
     */
    public <T> Mapper getMapper(Class<T> endpointType) {
        for (Class<?> key : endpointToTransfer.keySet()) {
            if (key.isAssignableFrom(endpointType)) {
                return mappers.get(key);
            }
        }
        return null;
    }

    /**
     * Converts the given object from its endpoint type to its transfer type.
     *
     * @param endpointValue
     *            the value returned from the endpoint
     * @return the value converted to its transfer type
     */
    public Object toTransferType(Object endpointValue) {
        if (endpointValue == null) {
            return null;
        }

        Class<?> endpointValueType = endpointValue.getClass();

        Mapper mapper = getMapper(endpointValueType);
        if (mapper == null) {
            return endpointValue;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                    "Mapping from endpoint type ({}) to transfer type ({})",
                    endpointValueType.getName(),
                    mapper.getTransferType().getName());
        }

        return mapper.toTransferType(endpointValue);
    }

    /**
     * Converts the given object from its transfer type to the given endpoint
     * type.
     *
     * @param transferValue
     *            the value transferred over the network
     * @param endpointType
     *            the value type declared in the endpoint, as parameter or
     *            return type
     * @param <T>
     *            the endpoint type
     * @return the value converted to its endpoint type
     */
    public <T> T toEndpointType(Object transferValue, Class<T> endpointType) {
        if (transferValue == null) {
            return null;
        }

        Mapper mapper = getMapper(endpointType);
        if (mapper == null) {
            return (T) transferValue;
        }

        getLogger().debug("Mapping from transfer type ("
                + transferValue.getClass().getName() + ") to endpoint type ("
                + endpointType.getName() + ")");

        return (T) mapper.toEndpointType(transferValue);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}
