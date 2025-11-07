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
package com.vaadin.hilla.parser.test.helpers.context;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

import io.github.classgraph.ScanResult;

public abstract class AbstractContext<ReflectionOrigin extends AnnotatedElement, SourceOrigin>
        extends BaseContext {
    private final Map<String, ReflectionOrigin> reflectionOrigins;
    private final Map<String, SourceOrigin> sourceOrigins;

    protected AbstractContext(ScanResult source,
            Map<String, ReflectionOrigin> reflectionOrigins,
            Map<String, SourceOrigin> sourceOrigins) {
        super(source);
        this.reflectionOrigins = reflectionOrigins;
        this.sourceOrigins = sourceOrigins;
    }

    public ReflectionOrigin getReflectionOrigin(String name) {
        return reflectionOrigins.get(name);
    }

    public Map<String, ReflectionOrigin> getReflectionOrigins() {
        return reflectionOrigins;
    }

    public SourceOrigin getSourceOrigin(String name) {
        return sourceOrigins.get(name);
    }

    public Map<String, SourceOrigin> getSourceOrigins() {
        return sourceOrigins;
    }
}
