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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel extends TypeParameterModel
        implements ReflectionSignatureModel {
    private final TypeVariable<?> origin;

    TypeParameterReflectionModel(TypeVariable<?> origin) {
        this.origin = origin;
    }

    @Override
    public TypeVariable<?> get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return Arrays.stream(origin.getAnnotations())
                .map(AnnotationInfoModel::of).collect(Collectors.toList());
    }

    @Override
    protected List<SignatureModel> prepareBounds() {
        return Arrays.stream(origin.getAnnotatedBounds())
                .map(SignatureModel::of).collect(Collectors.toList());
    }
}
