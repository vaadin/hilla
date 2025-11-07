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

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.AnnotatedModel;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.media.Schema;

public final class CompositeTypeSignatureNode extends
        AbstractNode<List<SignatureModel>, Schema<?>> implements TypedNode {

    private final List<AnnotationInfoModel> annotations;

    private CompositeTypeSignatureNode(@NonNull List<SignatureModel> source,
            @NonNull Schema<?> target, List<AnnotationInfoModel> annotations) {
        super(source, target);
        this.annotations = annotations;
    }

    private CompositeTypeSignatureNode(@NonNull List<SignatureModel> source,
            @NonNull Schema<?> target) {
        this(source, target, extractAnnotations(source));
    }

    @NonNull
    static public CompositeTypeSignatureNode of(
            @NonNull List<SignatureModel> source) {
        return new CompositeTypeSignatureNode(source, new Schema<>());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        return annotations;
    }

    @Override
    public SignatureModel getType() {
        return getSource().get(0);
    }

    @Override
    public TypedNode processType(
            @NonNull UnaryOperator<SignatureModel> typeProcessor) {
        var processedTypes = getSource().stream().map(typeProcessor)
                .collect(Collectors.toList());
        if (processedTypes.equals(getSource())) {
            return this;
        }

        return new CompositeTypeSignatureNode(processedTypes, getTarget(),
                annotations);
    }

    private static List<AnnotationInfoModel> extractAnnotations(
            List<SignatureModel> source) {
        return source.stream().map(AnnotatedModel::getAnnotations)
                .flatMap(Collection::stream).distinct()
                .collect(Collectors.toList());
    }
}
