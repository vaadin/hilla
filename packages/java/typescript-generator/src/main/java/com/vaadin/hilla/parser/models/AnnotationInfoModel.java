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

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.github.classgraph.AnnotationInfo;
import org.jspecify.annotations.NonNull;

public abstract class AnnotationInfoModel implements Model, NamedModel {
    private Optional<ClassInfoModel> classInfo;
    private Set<AnnotationParameterModel> parameters;

    @Deprecated
    public static AnnotationInfoModel of(@NonNull AnnotationInfo origin) {
        return new AnnotationInfoSourceModel(Objects.requireNonNull(origin));
    }

    public static AnnotationInfoModel of(@NonNull Annotation origin) {
        return new AnnotationInfoReflectionModel(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationInfoModel)) {
            return false;
        }

        var other = (AnnotationInfoModel) obj;

        return getName().equals(other.getName())
                && getParameters().equals(other.getParameters());
    }

    public Optional<ClassInfoModel> getClassInfo() {
        if (classInfo == null) {
            classInfo = prepareClassInfo();
        }

        return classInfo;
    }

    @Override
    public Class<AnnotationInfoModel> getCommonModelClass() {
        return AnnotationInfoModel.class;
    }

    public Set<AnnotationParameterModel> getParameters() {
        if (parameters == null) {
            parameters = prepareParameters();
        }

        return parameters;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 11 * getParameters().hashCode();
    }

    @Override
    public String toString() {
        return "AnnotationInfoModel[" + get() + "]";
    }

    protected abstract Optional<ClassInfoModel> prepareClassInfo();

    protected abstract Set<AnnotationParameterModel> prepareParameters();
}
