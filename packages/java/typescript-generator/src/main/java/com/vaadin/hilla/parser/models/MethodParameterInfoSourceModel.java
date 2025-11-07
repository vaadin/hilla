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

import java.util.List;

import io.github.classgraph.MethodParameterInfo;

final class MethodParameterInfoSourceModel extends MethodParameterInfoModel
        implements SourceModel {
    private final MethodParameterInfo origin;

    MethodParameterInfoSourceModel(MethodParameterInfo origin) {
        this.origin = origin;
    }

    @Override
    public MethodParameterInfo get() {
        return origin;
    }

    @Override
    public int getModifiers() {
        return origin.getModifiers();
    }

    @Override
    public String getName() {
        return origin.getName() == null ? "arg" + getIndex() : origin.getName();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
    }

    @Override
    public boolean isImplicit() {
        return origin.isMandated();
    }

    @Override
    public boolean isMandated() {
        return origin.isMandated();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotationInfo());
    }

    @Override
    protected int prepareIndex() {
        var parameters = origin.getMethodInfo().getParameterInfo();

        for (var i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(origin)) {
                return i;
            }
        }

        throw new IllegalStateException(
                "The parameter has not been found in the method parameter list");
    }

    @Override
    protected MethodInfoModel prepareOwner() {
        return MethodInfoModel.of(origin.getMethodInfo());
    }

    @Override
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor());
    }
}
