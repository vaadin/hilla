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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A hierarchy whose type ids come from the annotations, falling back to the
 * simple class name.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = SimpleBase.Nested.class),
        @JsonSubTypes.Type(value = SimpleBase.FromInterface.class),
        @JsonSubTypes.Type(value = SimpleBase.FromSuperclass.class) })
public class SimpleBase {
    public String id;

    /**
     * A subtype with no name anywhere: with {@code Id.SIMPLE_NAME}, its id is
     * the simple class name, whatever encloses it.
     */
    public static class Nested extends SimpleBase {
        public String note;
    }

    /**
     * Holds the name of the subtypes that implement it.
     */
    @JsonTypeName("from-interface")
    public interface Marker {
    }

    /**
     * Holds the name of the subtypes that extend it, and is not a subtype
     * itself.
     */
    @JsonTypeName("from-superclass")
    public static abstract class NamedParent extends SimpleBase {
        public int size;
    }

    /**
     * A subtype named by both an interface and a superclass: the interface
     * wins, as it does in Jackson.
     */
    public static class FromInterface extends NamedParent implements Marker {
        public String address;
    }

    /**
     * A subtype named by its superclass alone.
     */
    public static class FromSuperclass extends NamedParent {
        public String number;
    }
}
