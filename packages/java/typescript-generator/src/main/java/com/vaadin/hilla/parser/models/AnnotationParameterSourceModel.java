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

import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationEnumValue;
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
        var _value = origin.getValue();

        if (_value instanceof AnnotationClassRef) {
            var _ref = (AnnotationClassRef) _value;
            if (_ref.getClassInfo() == null) {
                // ClassGraph is missing the class, try loading from reflection
                return ClassInfoModel.of(_ref.loadClass());
            } else {
                return ClassInfoModel.of(_ref.getClassInfo());
            }
        } else if (_value instanceof AnnotationEnumValue) {
            return AnnotationParameterEnumValueModel
                    .of((AnnotationEnumValue) _value);
        }

        return _value;
    }
}
