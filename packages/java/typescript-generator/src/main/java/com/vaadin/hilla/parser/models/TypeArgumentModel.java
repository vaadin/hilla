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

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Objects;

import io.github.classgraph.TypeArgument;
import org.jspecify.annotations.NonNull;

public abstract class TypeArgumentModel extends AnnotatedAbstractModel
        implements SignatureModel {
    private List<SignatureModel> associatedTypes;

    @Deprecated
    public static TypeArgumentModel of(@NonNull TypeArgument origin) {
        return new TypeArgumentSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeArgumentModel of(@NonNull AnnotatedType origin) {
        return new TypeArgumentReflectionModel(Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeArgumentModel)) {
            return false;
        }

        var other = (TypeArgumentModel) obj;

        return getAnnotations().equals(other.getAnnotations())
                && getAssociatedTypes().equals(other.getAssociatedTypes())
                && getWildcard().equals(other.getWildcard());
    }

    public List<SignatureModel> getAssociatedTypes() {
        if (associatedTypes == null) {
            associatedTypes = prepareAssociatedTypes();
        }

        return associatedTypes;
    }

    @Override
    public Class<TypeArgumentModel> getCommonModelClass() {
        return TypeArgumentModel.class;
    }

    public abstract Wildcard getWildcard();

    @Override
    public int hashCode() {
        return getAssociatedTypes().hashCode() + 7 * getWildcard().hashCode();
    }

    @Override
    public boolean isTypeArgument() {
        return true;
    }

    @Override
    public String toString() {
        return "TypeArgumentModel[" + get() + "]";
    }

    protected abstract List<SignatureModel> prepareAssociatedTypes();

    public enum Wildcard {
        NONE, ANY, EXTENDS, SUPER;
    }
}
