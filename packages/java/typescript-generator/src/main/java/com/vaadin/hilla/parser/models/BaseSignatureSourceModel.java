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

import io.github.classgraph.BaseTypeSignature;

final class BaseSignatureSourceModel extends BaseSignatureModel
        implements SourceSignatureModel {
    private final BaseTypeSignature origin;

    BaseSignatureSourceModel(BaseTypeSignature origin) {
        this.origin = origin;
    }

    @Override
    public BaseTypeSignature get() {
        return origin;
    }

    @Override
    public Class<?> getType() {
        return origin.getType();
    }

    @Override
    public boolean isBoolean() {
        return origin.getType() == Boolean.TYPE;
    }

    @Override
    public boolean isByte() {
        return origin.getType() == Byte.TYPE;
    }

    @Override
    public boolean isCharacter() {
        return origin.getType() == Character.TYPE;
    }

    @Override
    public boolean isDouble() {
        return origin.getType() == Double.TYPE;
    }

    @Override
    public boolean isFloat() {
        return origin.getType() == Float.TYPE;
    }

    @Override
    public boolean isInteger() {
        return origin.getType() == Integer.TYPE;
    }

    @Override
    public boolean isJDKClass() {
        return true;
    }

    @Override
    public boolean isLong() {
        return origin.getType() == Long.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        var type = origin.getType();

        return type != null && type != Void.TYPE;
    }

    @Override
    public boolean isShort() {
        return origin.getType() == Short.TYPE;
    }

    @Override
    public boolean isVoid() {
        return origin.getType() == Void.TYPE;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getTypeAnnotationInfo());
    }
}
