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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.classgraph.TypeParameter;
import org.jspecify.annotations.NonNull;

public abstract class TypeParameterModel extends AnnotatedAbstractModel
        implements SignatureModel, NamedModel {
    /**
     * The origins of the type parameter pairs whose bounds are being compared
     * on the current thread. The origins are the key rather than the models
     * themselves, because a model is created anew every time a type parameter
     * is reached.
     *
     * @see #equals(Object)
     */
    private static final ThreadLocal<Set<List<Object>>> COMPARED_BOUNDS = ThreadLocal
            .withInitial(HashSet::new);

    private List<SignatureModel> bounds;

    @Deprecated
    public static TypeParameterModel of(@NonNull TypeParameter origin) {
        return new TypeParameterSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeParameterModel of(@NonNull TypeVariable<?> origin) {
        return new TypeParameterReflectionModel(Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeParameterModel)) {
            return false;
        }

        var other = (TypeParameterModel) obj;

        if (!getName().equals(other.getName())
                || !getAnnotations().equals(other.getAnnotations())) {
            return false;
        }

        var comparedBounds = COMPARED_BOUNDS.get();
        var pair = List.<Object> of(get(), other.get());

        // The bounds of an F-bounded type parameter, such as the
        // <E extends Enum<E>> of Enum itself, lead back to the type parameter,
        // which starts the very same comparison again. Meeting the same pair
        // twice means that everything on the way back to it has matched, which
        // is as far as the bounds can be compared, so they are equal.
        if (!comparedBounds.add(pair)) {
            return true;
        }

        try {
            return getBounds().equals(other.getBounds());
        } finally {
            comparedBounds.remove(pair);

            if (comparedBounds.isEmpty()) {
                COMPARED_BOUNDS.remove();
            }
        }
    }

    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = prepareBounds();
        }

        return bounds;
    }

    @Override
    public Class<TypeParameterModel> getCommonModelClass() {
        return TypeParameterModel.class;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 3 * getBounds().hashCode();
    }

    @Override
    public boolean isTypeParameter() {
        return true;
    }

    @Override
    public String toString() {
        return "TypeParameterModel[" + get() + "]";
    }

    protected abstract List<SignatureModel> prepareBounds();
}
