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

public abstract class AbstractCharacteristics<ReflectionOrigin extends AnnotatedElement, SourceOrigin>
        extends BaseContext {
    private final Map<ReflectionOrigin, String[]> reflectionCharacteristics;
    private final Map<SourceOrigin, String[]> sourceCharacteristics;

    public AbstractCharacteristics(ScanResult source,
            Map<ReflectionOrigin, String[]> reflectionCharacteristics,
            Map<SourceOrigin, String[]> sourceCharacteristics) {
        super(source);
        this.reflectionCharacteristics = reflectionCharacteristics;
        this.sourceCharacteristics = sourceCharacteristics;
    }

    public Map<ReflectionOrigin, String[]> getReflectionCharacteristics() {
        return reflectionCharacteristics;
    }

    public String[] getReflectionCharacteristicsPerOrigin(
            ReflectionOrigin origin) {
        return reflectionCharacteristics.get(origin);
    }

    public Map<SourceOrigin, String[]> getSourceCharacteristics() {
        return sourceCharacteristics;
    }

    public String[] getSourceCharacteristicsPerOrigin(SourceOrigin origin) {
        return sourceCharacteristics.get(origin);
    }
}
