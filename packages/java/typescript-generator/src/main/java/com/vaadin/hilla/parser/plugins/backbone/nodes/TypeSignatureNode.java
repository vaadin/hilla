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
package com.vaadin.hilla.parser.plugins.backbone.nodes;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.plugins.backbone.TypeFacts;
import com.vaadin.hilla.parser.utils.Signatures;

public class TypeSignatureNode extends AbstractNode<AnnotatedElement, TypeFacts>
        implements TypedNode {
    private final List<Annotation> annotations;
    private final Integer position;

    protected TypeSignatureNode(AnnotatedElement source, TypeFacts target,
            List<Annotation> annotations, Integer position) {
        super(source, target);
        this.annotations = annotations;
        this.position = position;
    }

    protected TypeSignatureNode(AnnotatedElement source, TypeFacts target,
            Integer position) {
        this(source, target, Signatures.getAnnotations(source), position);
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Integer getPosition() {
        return position;
    }

    public AnnotatedElement getType() {
        return getSource();
    }

    @Override
    public TypedNode processType(
            UnaryOperator<AnnotatedElement> typeProcessor) {
        var processedType = typeProcessor.apply(getSource());
        if (processedType.equals(getSource())) {
            return this;
        }

        return new TypeSignatureNode(typeProcessor.apply(getSource()),
                getTarget(), annotations, position);
    }

    @NonNull
    static public TypeSignatureNode of(@NonNull AnnotatedElement source) {
        return new TypeSignatureNode(source, TypeFacts.unknown(), null);
    }

    @NonNull
    static public TypeSignatureNode of(@NonNull AnnotatedElement source,
            int position) {
        return new TypeSignatureNode(source, TypeFacts.unknown(), position);
    }

    @Override
    public boolean equals(Object o) {
        boolean eq = super.equals(o);

        if (eq) {
            var other = (TypeSignatureNode) o;
            eq = Objects.equals(position, other.position);
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hashCode(position);
    }

    @Override
    public String toString() {
        var str = super.toString();

        if (position != null) {
            str += "[" + position + "]";
        }

        return str;
    }
}
