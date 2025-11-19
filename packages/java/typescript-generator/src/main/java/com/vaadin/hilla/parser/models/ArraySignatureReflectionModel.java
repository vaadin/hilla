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
import java.lang.reflect.AnnotatedElement;
import java.util.List;

final class ArraySignatureReflectionModel extends ArraySignatureModel
        implements ReflectionSignatureModel {
    private final AnnotatedArrayType origin;

    ArraySignatureReflectionModel(AnnotatedArrayType origin) {
        this.origin = origin;
    }

    @Override
    public AnnotatedElement get() {
        return origin;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected SignatureModel prepareNestedType() {
        return SignatureModel.of(origin.getAnnotatedGenericComponentType());
    }
}
