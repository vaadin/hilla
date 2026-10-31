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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

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
        return getClass().getSimpleName().replaceAll("Node$", "") + "("
                + getSourceName() + ")";
    }

    /**
     * The name of the source, as {@link #toString()} shows it.
     */
    protected String getSourceName() {
        return switch (source) {
        case Class<?> cls -> cls.getSimpleName();
        case Member member -> member.getName();
        case Parameter parameter -> parameter.getName();
        case AnnotatedElement signature -> signature.toString();
        default -> source.getClass().getSimpleName();
        };
    }
}
