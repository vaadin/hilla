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
package com.vaadin.hilla.generator.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything one run of the parser found, which is what the TypeScript of an
 * application is written from.
 *
 * @param endpoints
 *            the browser callable classes
 * @param entities
 *            the types those refer to, which are generated as declarations of
 *            their own
 * @param unions
 *            the polymorphic ones among them, with the subtypes a value can be
 * @param files
 *            the files the plugins already rendered while the walk ran, by
 *            their path relative to the output folder
 */
public record Generation(List<EndpointModel> endpoints,
        List<EntityModel> entities, List<UnionModel> unions,
        Map<String, String> files) {
    public Generation {
        endpoints = List.copyOf(endpoints);
        entities = List.copyOf(entities);
        unions = List.copyOf(unions);
        files = Collections.unmodifiableMap(new LinkedHashMap<>(files));
    }

    /**
     * What a walk found, without any file rendered while it ran.
     */
    public Generation(List<EndpointModel> endpoints, List<EntityModel> entities,
            List<UnionModel> unions) {
        this(endpoints, entities, unions, Map.of());
    }

    /**
     * The same generation, with the given files rendered while the walk ran.
     */
    public Generation withFiles(Map<String, String> files) {
        return new Generation(endpoints, entities, unions, files);
    }
}
