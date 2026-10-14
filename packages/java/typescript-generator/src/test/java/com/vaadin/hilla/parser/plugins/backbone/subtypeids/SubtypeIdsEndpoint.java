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
package com.vaadin.hilla.parser.plugins.backbone.subtypeids;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Returns polymorphic types whose ids are given as a list of names, or written
 * around the value rather than as a property of it.
 */
@Endpoint
public class SubtypeIdsEndpoint {
    public Pet getPet() {
        return new Cat();
    }

    public Wrapped getWrapped() {
        return new Inner();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Cat.class, names = { "cat", "kitty" }) })
    public static class Pet {
        public String name;
    }

    public static class Cat extends Pet {
        public boolean indoor;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({ @JsonSubTypes.Type(value = Inner.class, name = "inner") })
    public static class Wrapped {
    }

    public static class Inner extends Wrapped {
        public String value;
    }
}
