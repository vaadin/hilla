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
import java.lang.reflect.AnnotatedWildcardType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.hilla.parser.utils.Streams;

final class TypeArgumentReflectionModel extends TypeArgumentModel
        implements ReflectionSignatureModel {
    private final AnnotatedType origin;
    private Wildcard wildcard;

    TypeArgumentReflectionModel(AnnotatedType origin) {
        this.origin = origin;
    }

    private static boolean isNonNativeObjectType(AnnotatedType type) {
        return type.getType() != Object.class;
    }

    @Override
    public AnnotatedType get() {
        return origin;
    }

    @Override
    public Wildcard getWildcard() {
        if (wildcard == null) {
            if (origin instanceof AnnotatedWildcardType) {
                var specific = (AnnotatedWildcardType) origin;

                if (specific.getAnnotatedLowerBounds().length > 0) {
                    wildcard = Wildcard.SUPER;
                } else if (!specific.getAnnotatedUpperBounds()[0].getType()
                        .equals(Object.class)) {
                    wildcard = Wildcard.EXTENDS;
                } else {
                    wildcard = Wildcard.ANY;
                }
            } else {
                wildcard = Wildcard.NONE;
            }
        }

        return wildcard;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected List<SignatureModel> prepareAssociatedTypes() {
        var stream = origin instanceof AnnotatedWildcardType ? Streams.combine(
                ((AnnotatedWildcardType) origin).getAnnotatedLowerBounds(),
                ((AnnotatedWildcardType) origin).getAnnotatedUpperBounds())
                : Stream.of(origin);

        return stream.filter(TypeArgumentReflectionModel::isNonNativeObjectType)
                .map(SignatureModel::of).distinct()
                .collect(Collectors.toList());
    }
}
