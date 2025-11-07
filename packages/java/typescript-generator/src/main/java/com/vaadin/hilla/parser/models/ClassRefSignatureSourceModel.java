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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.TypeArgument;

abstract class ClassRefSignatureSourceModel extends ClassRefSignatureModel
        implements SourceSignatureModel {
    protected final ClassRefTypeSignature origin;

    ClassRefSignatureSourceModel(ClassRefTypeSignature origin) {
        this.origin = origin;
    }

    @Override
    public ClassRefTypeSignature get() {
        return origin;
    }

    protected List<AnnotationInfo> getOriginAnnotations() {
        return origin.getTypeAnnotationInfo();
    }

    protected ClassInfo getOriginClassInfo() {
        return origin.getClassInfo();
    }

    protected List<TypeArgument> getOriginTypeArguments() {
        return origin.getTypeArguments();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(getOriginAnnotations());
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return origin.getBaseClassName().equals("java.lang.Object")
                ? ClassInfoModel.of(Object.class)
                : ClassInfoModel.of(getOriginClassInfo());
    }

    @Override
    protected Optional<ClassRefSignatureModel> prepareOwner() {
        return Optional.empty();
    }

    @Override
    protected List<TypeArgumentModel> prepareTypeArguments() {
        return getOriginTypeArguments().stream().map(TypeArgumentModel::of)
                .collect(Collectors.toList());
    }

    static final class Regular extends ClassRefSignatureSourceModel {
        public Regular(ClassRefTypeSignature origin) {
            super(origin);
        }
    }

    static final class Suffixed extends ClassRefSignatureSourceModel {
        private final int currentSuffixIndex;

        Suffixed(ClassRefTypeSignature origin) {
            this(origin, origin.getSuffixes().size() - 1);
        }

        Suffixed(ClassRefTypeSignature origin, int currentSuffixIndex) {
            super(origin);
            this.currentSuffixIndex = currentSuffixIndex;
        }

        @Override
        protected List<AnnotationInfo> getOriginAnnotations() {
            var suffixAnnotations = origin.getSuffixTypeAnnotationInfo();

            return suffixAnnotations != null
                    ? origin.getSuffixTypeAnnotationInfo()
                            .get(currentSuffixIndex)
                    : null;
        }

        @Override
        protected ClassInfo getOriginClassInfo() {
            if (currentSuffixIndex == origin.getSuffixes().size() - 1) {
                return origin.getClassInfo();
            }

            var outerClasses = origin.getClassInfo().getOuterClasses();
            var currentSuffix = origin.getSuffixes().get(currentSuffixIndex);

            for (var cls : outerClasses) {
                if (cls.getName().endsWith(currentSuffix)) {
                    return cls;
                }
            }

            throw new NoSuchElementException();
        }

        @Override
        protected List<TypeArgument> getOriginTypeArguments() {
            return origin.getSuffixTypeArguments().get(currentSuffixIndex);
        }

        @Override
        protected Optional<ClassRefSignatureModel> prepareOwner() {
            return currentSuffixIndex > 0
                    ? Optional.of(new Suffixed(origin, currentSuffixIndex - 1))
                    : Optional.of(new SuffixedBase(origin));
        }
    }

    static final class SuffixedBase extends ClassRefSignatureSourceModel {
        SuffixedBase(ClassRefTypeSignature origin) {
            super(origin);
        }

        @Override
        protected ClassInfo getOriginClassInfo() {
            var outerClasses = origin.getClassInfo().getOuterClasses();
            return outerClasses.get(outerClasses.size() - 1);
        }
    }
}
