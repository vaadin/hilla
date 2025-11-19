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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.MethodInfo;

final class MethodInfoSourceModel extends MethodInfoModel
        implements SourceModel {
    private final MethodInfo origin;

    MethodInfoSourceModel(MethodInfo origin) {
        this.origin = origin;
    }

    @Override
    public MethodInfo get() {
        return origin;
    }

    @Override
    public String getClassName() {
        return origin.getClassName();
    }

    @Override
    public int getModifiers() {
        return origin.getModifiers();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public boolean isAbstract() {
        return origin.isAbstract();
    }

    @Override
    public boolean isBridge() {
        return origin.isBridge();
    }

    @Override
    public boolean isConstructor() {
        return origin.isConstructor();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
    }

    @Override
    public boolean isNative() {
        return origin.isNative();
    }

    @Override
    public boolean isPrivate() {
        return origin.isPrivate();
    }

    @Override
    public boolean isProtected() {
        return origin.isProtected();
    }

    @Override
    public boolean isPublic() {
        return origin.isPublic();
    }

    @Override
    public boolean isStatic() {
        return origin.isStatic();
    }

    @Override
    public boolean isStrict() {
        return origin.isStrict();
    }

    @Override
    public boolean isSynchronized() {
        return origin.isSynchronized();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isVarArgs() {
        return origin.isVarArgs();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotationInfo());
    }

    @Override
    protected ClassInfoModel prepareOwner() {
        return ClassInfoModel.of(origin.getClassInfo());
    }

    @Override
    protected List<MethodParameterInfoModel> prepareParameters() {
        return Arrays.stream(origin.getParameterInfo())
                .map(MethodParameterInfoModel::of).collect(Collectors.toList());
    }

    @Override
    protected SignatureModel prepareResultType() {
        return SignatureModel
                .of(origin.getTypeSignatureOrTypeDescriptor().getResultType());
    }

    @Override
    protected List<TypeParameterModel> prepareTypeParameters() {
        return origin.getTypeSignatureOrTypeDescriptor().getTypeParameters()
                .stream().map(TypeParameterModel::of)
                .collect(Collectors.toList());
    }
}
