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
import java.util.List;

final class BaseSignatureReflectionModel extends BaseSignatureModel
        implements ReflectionSignatureModel {
    private final Class<?> inner;
    private final AnnotatedType origin;

    BaseSignatureReflectionModel(AnnotatedType origin) {
        this.origin = origin;
        this.inner = (Class<?>) origin.getType();
    }

    @Override
    public AnnotatedType get() {
        return origin;
    }

    @Override
    public Class<?> getType() {
        return inner;
    }

    @Override
    public boolean isBoolean() {
        return inner == Boolean.TYPE;
    }

    @Override
    public boolean isByte() {
        return inner == Byte.TYPE;
    }

    @Override
    public boolean isCharacter() {
        return inner == Character.TYPE;
    }

    @Override
    public boolean isDouble() {
        return inner == Double.TYPE;
    }

    @Override
    public boolean isFloat() {
        return inner == Float.TYPE;
    }

    @Override
    public boolean isInteger() {
        return inner == Integer.TYPE;
    }

    @Override
    public boolean isJDKClass() {
        return true;
    }

    @Override
    public boolean isLong() {
        return inner == Long.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        return inner != Void.TYPE;
    }

    @Override
    public boolean isShort() {
        return inner == Short.TYPE;
    }

    @Override
    public boolean isVoid() {
        return inner == Void.TYPE;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    static class Bare extends BaseSignatureModel
            implements ReflectionSignatureModel {
        private final Class<?> origin;

        public Bare(Class<?> origin) {
            this.origin = origin;
        }

        @Override
        public Class<?> get() {
            return origin;
        }

        @Override
        public Class<?> getType() {
            return origin;
        }

        @Override
        public boolean isBoolean() {
            return origin == Boolean.TYPE;
        }

        @Override
        public boolean isByte() {
            return origin == Byte.TYPE;
        }

        @Override
        public boolean isCharacter() {
            return origin == Character.TYPE;
        }

        @Override
        public boolean isDouble() {
            return origin == Double.TYPE;
        }

        @Override
        public boolean isFloat() {
            return origin == Float.TYPE;
        }

        @Override
        public boolean isInteger() {
            return origin == Integer.TYPE;
        }

        @Override
        public boolean isJDKClass() {
            return true;
        }

        @Override
        public boolean isLong() {
            return origin == Long.TYPE;
        }

        @Override
        public boolean isPrimitive() {
            return origin != Void.TYPE;
        }

        @Override
        public boolean isShort() {
            return origin == Short.TYPE;
        }

        @Override
        public boolean isVoid() {
            return origin == Void.TYPE;
        }

        @Override
        protected List<AnnotationInfoModel> prepareAnnotations() {
            return processAnnotations(origin.getAnnotations());
        }
    }
}
