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

import java.util.List;

/**
 * Everything one run of the parser found, which is what the TypeScript of an
 * application is written from.
 *
 * <p>
 * <b>Internal API.</b> Not part of the public Hilla API: used only by Hilla
 * itself, and may change or be removed in any release without notice or
 * deprecation. Do not use it in application code or libraries.
 *
 * @param endpoints
 *            the browser callable classes
 * @param entities
 *            the types those refer to, which are generated as declarations of
 *            their own
 * @param unions
 *            the polymorphic ones among them, with the subtypes a value can be
 */
public record Generation(List<EndpointModel> endpoints,
        List<EntityModel> entities, List<UnionModel> unions) {
    public Generation {
        endpoints = List.copyOf(endpoints);
        entities = List.copyOf(entities);
        unions = List.copyOf(unions);
    }
}
