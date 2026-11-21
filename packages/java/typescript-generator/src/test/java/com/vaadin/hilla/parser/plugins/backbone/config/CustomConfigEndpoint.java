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
package com.vaadin.hilla.parser.plugins.backbone.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class CustomConfigEndpoint {
    public CustomConfigEntity get() {
        return new CustomConfigEntity();
    }

    public ConfiguredKindBase getConfiguredKind() {
        return new ConfiguredKindSub();
    }

    public static class CustomConfigEntity {
        private int bar;
        private String foo;
    }

    public enum Kind {
        SUB
    }

    /**
     * Declares its discriminator as an enum in a field only the custom factory
     * makes visible, so the hierarchy has to be read with that factory to leave
     * the discriminator to what it is declared as.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "kind")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ConfiguredKindSub.class, name = "SUB") })
    public static class ConfiguredKindBase {
        private Kind kind;
    }

    public static class ConfiguredKindSub extends ConfiguredKindBase {
        private String name;
    }
}
