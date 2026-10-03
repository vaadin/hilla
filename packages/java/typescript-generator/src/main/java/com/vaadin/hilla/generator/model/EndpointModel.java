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
import java.util.Objects;

/**
 * A browser callable class as the TypeScript generator needs it: the name the
 * client calls it by, and the methods it exposes.
 *
 * @param name
 *            the name used both for the generated file and in the calls to the
 *            server, which is not necessarily the simple name of the Java class
 * @param javaClass
 *            the fully qualified name of the Java class
 */
public record EndpointModel(String name, String javaClass,
        List<MethodModel> methods) {
    public EndpointModel {
        Objects.requireNonNull(name);
        Objects.requireNonNull(javaClass);
        methods = List.copyOf(methods);
    }
}
