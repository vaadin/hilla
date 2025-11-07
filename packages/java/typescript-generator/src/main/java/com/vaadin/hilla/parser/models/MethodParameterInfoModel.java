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

import java.lang.reflect.Parameter;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.github.classgraph.MethodParameterInfo;

public abstract class MethodParameterInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel, OwnedModel<MethodInfoModel> {
    private Integer index;
    private MethodInfoModel owner;
    private SignatureModel type;

    @Deprecated
    public static MethodParameterInfoModel of(
            @NonNull MethodParameterInfo origin) {
        return new MethodParameterInfoSourceModel(
                Objects.requireNonNull(origin));
    }

    public static MethodParameterInfoModel of(@NonNull Parameter origin) {
        return new MethodParameterInfoReflectionModel(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MethodParameterInfoModel)) {
            return false;
        }

        var other = (MethodParameterInfoModel) obj;

        return getOwner().equalsIgnoreParameters(other.getOwner())
                && getAnnotations().equals(other.getAnnotations())
                && getModifiers() == other.getModifiers()
                && getType().equals(other.getType())
                && getName().equals(other.getName());
    }

    @Override
    public Class<MethodParameterInfoModel> getCommonModelClass() {
        return MethodParameterInfoModel.class;
    }

    /**
     * Returns a zero-based index of the parameter in the method parameter list.
     *
     * @return the index, from 0 to n-1
     */
    public int getIndex() {
        if (index == null) {
            index = prepareIndex();
        }

        return index;
    }

    public abstract int getModifiers();

    @Override
    public MethodInfoModel getOwner() {
        if (owner == null) {
            owner = prepareOwner();
        }

        return owner;
    }

    public SignatureModel getType() {
        if (type == null) {
            type = prepareType();
        }

        return type;
    }

    @Override
    public int hashCode() {
        return getOwner().hashCodeIgnoreParameters()
                + 11 * getAnnotations().hashCode() + 17 * getModifiers()
                + 23 * getType().hashCode() + 53 * getName().hashCode();
    }

    public abstract boolean isFinal();

    public abstract boolean isImplicit();

    public abstract boolean isMandated();

    public abstract boolean isSynthetic();

    @Override
    public String toString() {
        return "MethodParameterInfoModel[" + get() + "]";
    }

    protected abstract int prepareIndex();

    protected abstract MethodInfoModel prepareOwner();

    protected abstract SignatureModel prepareType();
}
