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
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.github.classgraph.BaseTypeSignature;

public abstract class BaseSignatureModel extends AnnotatedAbstractModel
        implements SignatureModel {
    @Deprecated
    public static BaseSignatureModel of(@NonNull BaseTypeSignature origin) {
        return new BaseSignatureSourceModel(Objects.requireNonNull(origin));
    }

    public static BaseSignatureModel of(@NonNull AnnotatedType origin) {
        return new BaseSignatureReflectionModel(Objects.requireNonNull(origin));
    }

    public static BaseSignatureModel of(@NonNull Class<?> origin) {
        return new BaseSignatureReflectionModel.Bare(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BaseSignatureModel)) {
            return false;
        }

        var other = (BaseSignatureModel) obj;

        return getType().equals(other.getType())
                && Objects.equals(getAnnotations(), other.getAnnotations());
    }

    @Override
    public Class<BaseSignatureModel> getCommonModelClass() {
        return BaseSignatureModel.class;
    }

    public abstract Class<?> getType();

    @Override
    public int hashCode() {
        return 7 + getType().hashCode();
    }

    @Override
    public boolean isBase() {
        return true;
    }

    @Override
    public String toString() {
        return "BaseSignatureModel[" + get() + "]";
    }
}
