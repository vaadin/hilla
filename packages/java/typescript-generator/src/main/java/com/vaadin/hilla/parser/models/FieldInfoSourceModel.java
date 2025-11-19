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

import io.github.classgraph.FieldInfo;

final class FieldInfoSourceModel extends FieldInfoModel implements SourceModel {
    private final FieldInfo origin;

    FieldInfoSourceModel(FieldInfo origin) {
        this.origin = origin;
    }

    @Override
    public FieldInfo get() {
        return origin;
    }

    @Override
    public String getClassName() {
        return origin.getClassName();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public boolean isEnum() {
        return origin.isEnum();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
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
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isTransient() {
        return origin.isTransient();
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
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor());
    }
}
