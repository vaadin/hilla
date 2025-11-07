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
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * An artificial ClassRefSignatureModel implementation.
 */
final class ClassRefSignatureArtificialModel extends ClassRefSignatureModel {
    private final List<AnnotationInfoModel> annotations;
    private final ClassInfoModel classInfo;
    private final List<TypeArgumentModel> typeArguments;

    ClassRefSignatureArtificialModel(@NonNull ClassInfoModel classInfo,
            @NonNull List<TypeArgumentModel> typeArguments,
            @NonNull List<AnnotationInfoModel> annotations) {
        this.classInfo = Objects.requireNonNull(classInfo);
        this.typeArguments = Objects.requireNonNull(typeArguments);
        this.annotations = Objects.requireNonNull(annotations);
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return annotations;
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return classInfo;
    }

    @Override
    protected Optional<ClassRefSignatureModel> prepareOwner() {
        return Optional.empty();
    }

    @Override
    protected List<TypeArgumentModel> prepareTypeArguments() {
        return typeArguments;
    }
}
