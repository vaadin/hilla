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
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.plugins.backbone.TypeFacts;
import com.vaadin.hilla.parser.utils.Signatures;

public final class CompositeTypeSignatureNode extends
        AbstractNode<List<AnnotatedElement>, TypeFacts> implements TypedNode {

    private final List<Annotation> annotations;

    private CompositeTypeSignatureNode(@NonNull List<AnnotatedElement> source,
            @NonNull TypeFacts target, List<Annotation> annotations) {
        super(source, target);
        this.annotations = annotations;
    }

    private CompositeTypeSignatureNode(@NonNull List<AnnotatedElement> source,
            @NonNull TypeFacts target) {
        this(source, target, extractAnnotations(source));
    }

    @NonNull
    static public CompositeTypeSignatureNode of(
            @NonNull List<AnnotatedElement> source) {
        return new CompositeTypeSignatureNode(source, TypeFacts.unknown());
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public AnnotatedElement getType() {
        return getSource().get(0);
    }

    @Override
    public TypedNode processType(
            @NonNull UnaryOperator<AnnotatedElement> typeProcessor) {
        var processedTypes = getSource().stream().map(typeProcessor)
                .collect(Collectors.toList());
        if (processedTypes.equals(getSource())) {
            return this;
        }

        return new CompositeTypeSignatureNode(processedTypes, getTarget(),
                annotations);
    }

    private static List<Annotation> extractAnnotations(
            List<AnnotatedElement> source) {
        return source.stream().map(Signatures::getAnnotations)
                .flatMap(Collection::stream).distinct()
                .collect(Collectors.toList());
    }
}
