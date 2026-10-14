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
package com.vaadin.hilla.parser.plugins.nonnull.genericentity;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Uses a generic entity where the entity and the type it is given are not
 * equally always there, which are two separate questions.
 */
@Endpoint
public class GenericEntityEndpoint {
    public @NonNull Box<String> boxOfMaybe() {
        return new Box<>();
    }

    public Box<@NonNull String> maybeBoxOf() {
        return null;
    }

    public Holder getHolder() {
        return new Holder();
    }

    public static class Box<T> {
        public T content;
    }

    public static class Holder {
        public @NonNull Box<String> boxOfMaybe;
        public Box<@NonNull String> maybeBoxOf;
    }
}
