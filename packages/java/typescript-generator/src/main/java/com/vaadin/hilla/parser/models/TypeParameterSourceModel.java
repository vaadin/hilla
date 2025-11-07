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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.TypeParameter;
import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.utils.Streams;

final class TypeParameterSourceModel extends TypeParameterModel
        implements SourceSignatureModel {
    private final TypeParameter origin;

    TypeParameterSourceModel(TypeParameter origin) {
        this.origin = origin;
    }

    @Override
    public TypeParameter get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();
        return annotations == null ? List.of()
                : annotations.stream().map(AnnotationInfoModel::of)
                        .collect(Collectors.toList());
    }

    @Override
    protected List<SignatureModel> prepareBounds() {
        return Streams
                .combine(Stream.of(getClassBoundSignature()),
                        origin.getInterfaceBounds().stream()
                                .map(SignatureModel::of))
                .distinct().collect(Collectors.toList());
    }

    @NonNull
    private SignatureModel getClassBoundSignature() {
        // FIXME: param class bound is sometimes null and sometimes Object.
        // Possibly a bug in ClassGraph. Use Object to align with reflection.
        var classBound = origin.getClassBound();
        if (classBound == null) {
            return ClassRefSignatureModel.of(Object.class);
        }

        return SignatureModel.of(classBound);
    }
}
