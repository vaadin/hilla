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

import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.github.classgraph.AnnotationEnumValue;

public abstract class AnnotationParameterEnumValueModel implements Model {
    private ClassInfoModel classInfo;

    @Deprecated
    public static AnnotationParameterEnumValueModel of(
            @NonNull AnnotationEnumValue origin) {
        return new AnnotationParameterEnumValueSourceModel(
                Objects.requireNonNull(origin));
    }

    public static AnnotationParameterEnumValueModel of(
            @NonNull Enum<?> origin) {
        return new AnnotationParameterEnumValueReflectionModel(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationParameterEnumValueModel)) {
            return false;
        }

        var other = (AnnotationParameterEnumValueModel) obj;

        return getClassInfo().equals(other.getClassInfo())
                && getValueName().equals(other.getValueName());
    }

    public ClassInfoModel getClassInfo() {
        if (classInfo == null) {
            classInfo = prepareClassInfo();
        }

        return classInfo;
    }

    @Override
    public Class<AnnotationParameterEnumValueModel> getCommonModelClass() {
        return AnnotationParameterEnumValueModel.class;
    }

    public abstract String getValueName();

    @Override
    public int hashCode() {
        return getClassInfo().hashCode() + 13 * getValueName().hashCode();
    }

    @Override
    public String toString() {
        return "AnnotationParameterEnumValueModel[" + get() + "]";
    }

    protected abstract ClassInfoModel prepareClassInfo();
}
