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

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Objects;

import io.github.classgraph.TypeVariableSignature;
import org.jspecify.annotations.NonNull;

public abstract class TypeVariableModel extends AnnotatedAbstractModel
        implements SignatureModel, NamedModel {
    private TypeParameterModel typeParameter;

    @Deprecated
    public static TypeVariableModel of(@NonNull TypeVariableSignature origin) {
        return new TypeVariableSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeVariableModel of(@NonNull AnnotatedTypeVariable origin) {
        return new TypeVariableReflectionModel(Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeVariableModel)) {
            return false;
        }

        var other = (TypeVariableModel) obj;

        return getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations())
                && resolve().equals(other.resolve());
    }

    @Override
    public Class<TypeVariableModel> getCommonModelClass() {
        return TypeVariableModel.class;
    }

    @Override
    public int hashCode() {
        return 0x4f76c9f1 ^ getName().hashCode();
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }

    public TypeParameterModel resolve() {
        if (typeParameter == null) {
            typeParameter = prepareResolved();
        }

        return typeParameter;
    }

    @Override
    public String toString() {
        return "TypeVariableModel[" + get() + "]";
    }

    protected abstract TypeParameterModel prepareResolved();
}
