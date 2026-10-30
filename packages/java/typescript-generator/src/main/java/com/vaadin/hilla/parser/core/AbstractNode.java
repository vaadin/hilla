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
package com.vaadin.hilla.parser.core;

import java.util.Objects;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.NamedModel;
import com.vaadin.hilla.parser.models.SignatureModel;

public abstract class AbstractNode<S, T> implements Node<S, T> {
    private final S source;

    private T target;

    protected AbstractNode(@NonNull S source, T target) {
        this.source = Objects.requireNonNull(source);
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var node = (AbstractNode<?, ?>) o;
        return source.equals(node.getSource());
    }

    @Override
    public S getSource() {
        return source;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public void setTarget(T target) {
        this.target = target;
    }

    @Override
    public int hashCode() {
        return source.hashCode() ^ 0x042ebeb0;
    }

    @Override
    public String toString() {
        var sourceName = "";
        if (source instanceof ClassInfoModel) {
            sourceName = ((ClassInfoModel) source).getSimpleName();
        } else if (source instanceof SignatureModel) {
            sourceName = source.toString();
        } else if (source instanceof NamedModel) {
            sourceName = ((NamedModel) source).getName();
        } else {
            sourceName = source.getClass().getSimpleName();
        }

        return getClass().getSimpleName().replaceAll("Node$", "") + "("
                + sourceName + ")";
    }
}
