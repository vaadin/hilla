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

import java.lang.reflect.AnnotatedArrayType;
import java.util.Objects;

import io.github.classgraph.ArrayTypeSignature;
import org.jspecify.annotations.NonNull;

public abstract class ArraySignatureModel extends AnnotatedAbstractModel
        implements SignatureModel {
    private SignatureModel nestedType;

    @Deprecated
    public static ArraySignatureModel of(@NonNull ArrayTypeSignature origin) {
        return new ArraySignatureSourceModel(Objects.requireNonNull(origin));
    }

    public static ArraySignatureModel of(@NonNull AnnotatedArrayType origin) {
        return new ArraySignatureReflectionModel(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ArraySignatureModel)) {
            return false;
        }

        var other = (ArraySignatureModel) obj;

        return getNestedType().equals(other.getNestedType())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public Class<ArraySignatureModel> getCommonModelClass() {
        return ArraySignatureModel.class;
    }

    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = prepareNestedType();
        }

        return nestedType;
    }

    @Override
    public int hashCode() {
        return 1 + getNestedType().hashCode();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public String toString() {
        return "ArraySignatureModel[" + get() + "]";
    }

    protected abstract SignatureModel prepareNestedType();
}
