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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class AnnotationInfoReflectionModel extends AnnotationInfoModel
        implements ReflectionModel {
    private final Annotation origin;

    AnnotationInfoReflectionModel(Annotation origin) {
        this.origin = origin;
    }

    @Override
    public Annotation get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.annotationType().getName();
    }

    @Override
    protected Optional<ClassInfoModel> prepareClassInfo() {
        return Optional.of(ClassInfoModel.of(origin.annotationType()));
    }

    @Override
    protected Set<AnnotationParameterModel> prepareParameters() {
        return Arrays.stream(origin.annotationType().getDeclaredMethods())
                .map(method -> {
                    // Here we go through all the methods/parameters of the
                    // annotation instance and collect their values. Since
                    // annotations methods cannot be private or virtual, we
                    // could simply invoke the method to get a value.
                    try {
                        var value = method.invoke(origin);
                        var isDefault = Objects
                                .deepEquals(method.getDefaultValue(), value);
                        return AnnotationParameterModel.of(method.getName(),
                                value, isDefault);
                    } catch (InvocationTargetException
                            | IllegalAccessException e) {
                        throw new ModelException(e);
                    }
                }).collect(Collectors.toSet());
    }
}
