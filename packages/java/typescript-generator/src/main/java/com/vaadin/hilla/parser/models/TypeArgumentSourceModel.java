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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.TypeArgument;

final class TypeArgumentSourceModel extends TypeArgumentModel
        implements SourceSignatureModel {
    private final TypeArgument origin;

    TypeArgumentSourceModel(TypeArgument origin) {
        this.origin = origin;
    }

    @Override
    public TypeArgument get() {
        return origin;
    }

    @Override
    public Wildcard getWildcard() {
        switch (origin.getWildcard()) {
        case EXTENDS:
            return Wildcard.EXTENDS;
        case ANY:
            return Wildcard.ANY;
        case SUPER:
            return Wildcard.SUPER;
        default:
            return Wildcard.NONE;
        }
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();

        return Stream.concat(
                getAssociatedTypes().stream()
                        .map(SignatureModel::getAnnotations)
                        .flatMap(Collection::stream),
                annotations != null
                        ? annotations.stream().map(AnnotationInfoModel::of)
                        : Stream.empty())
                .collect(Collectors.toList());
    }

    @Override
    protected List<SignatureModel> prepareAssociatedTypes() {
        var signature = origin.getTypeSignature();

        return signature == null ? List.of()
                : List.of(SignatureModel.of(signature));
    }
}
