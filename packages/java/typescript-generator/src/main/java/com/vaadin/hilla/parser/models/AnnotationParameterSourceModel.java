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
package com.vaadin.hilla.parser.models;

import java.util.Arrays;

import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;

final class AnnotationParameterSourceModel extends AnnotationParameterModel
        implements SourceModel {
    private final AnnotationParameterValue origin;

    AnnotationParameterSourceModel(AnnotationParameterValue origin) {
        this.origin = origin;
    }

    @Override
    public AnnotationParameterValue get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    protected Object prepareValue() {
        return convert(origin.getValue());
    }

    /**
     * Replaces the ClassGraph objects an annotation attribute may hold with the
     * models of this package, recursing into arrays so that an element gets the
     * same treatment as a value of its own. Arrays become lists, which gives
     * {@link AnnotationParameterModel#equals(Object)} value semantics.
     */
    private static Object convert(Object value) {
        return switch (value) {
        case AnnotationClassRef ref ->
            ref.getClassInfo() != null ? ClassInfoModel.of(ref.getClassInfo())
                    // ClassGraph is missing the class, load it by reflection
                    : ClassInfoModel.of(ref.loadClass());
        case AnnotationEnumValue enumValue ->
            AnnotationParameterEnumValueModel.of(enumValue);
        case AnnotationInfo annotation -> AnnotationInfoModel.of(annotation);
        case Object[] array -> Arrays.stream(array)
                .map(AnnotationParameterSourceModel::convert).toList();
        default -> value;
        };
    }
}
