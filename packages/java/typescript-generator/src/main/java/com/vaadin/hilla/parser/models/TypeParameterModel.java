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

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.github.classgraph.TypeParameter;

public abstract class TypeParameterModel extends AnnotatedAbstractModel
        implements SignatureModel, NamedModel {
    private List<SignatureModel> bounds;

    @Deprecated
    public static TypeParameterModel of(@NonNull TypeParameter origin) {
        return new TypeParameterSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeParameterModel of(@NonNull TypeVariable<?> origin) {
        return new TypeParameterReflectionModel(Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeParameterModel)) {
            return false;
        }

        var other = (TypeParameterModel) obj;

        return getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations())
                && getBounds().equals(other.getBounds());
    }

    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = prepareBounds();
        }

        return bounds;
    }

    @Override
    public Class<TypeParameterModel> getCommonModelClass() {
        return TypeParameterModel.class;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 3 * getBounds().hashCode();
    }

    @Override
    public boolean isTypeParameter() {
        return true;
    }

    @Override
    public String toString() {
        return "TypeParameterModel[" + get() + "]";
    }

    protected abstract List<SignatureModel> prepareBounds();
}
