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
package com.vaadin.hilla.parser.plugins.backbone.boundedmethod;

import java.util.List;
import java.util.Optional;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Declares type parameters of its methods bound to an entity, and an entity
 * holding an optional value of its own type parameter.
 */
@Endpoint
public class BoundedMethodEndpoint {
    public <T extends Item> T first(List<T> items) {
        return items.get(0);
    }

    public Holder<String> getHolder() {
        return new Holder<>();
    }

    public static class Item {
        public String name;
    }

    public static class Holder<T> {
        public T value;
        public Optional<T> maybe;
    }
}
