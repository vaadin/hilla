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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

final class FieldInfoReflectionModel extends FieldInfoModel
        implements ReflectionModel {
    private final Field origin;

    FieldInfoReflectionModel(Field origin) {
        this.origin = origin;
    }

    @Override
    public Field get() {
        return origin;
    }

    @Override
    public String getClassName() {
        return origin.getDeclaringClass().getName();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public boolean isEnum() {
        return origin.isEnumConstant();
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(origin.getModifiers());
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(origin.getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(origin.getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(origin.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(origin.getModifiers());
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isTransient() {
        return Modifier.isTransient(origin.getModifiers());
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected ClassInfoModel prepareOwner() {
        return ClassInfoModel.of(origin.getDeclaringClass());
    }

    @Override
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.getAnnotatedType());
    }
}
