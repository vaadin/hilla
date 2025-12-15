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

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.SignatureModel;

public class TypeSignatureNode extends AbstractNode<SignatureModel, Schema<?>>
        implements TypedNode {
    private final List<AnnotationInfoModel> annotations;
    private final Integer position;

    protected TypeSignatureNode(SignatureModel source, Schema<?> target,
            List<AnnotationInfoModel> annotations, Integer position) {
        super(source, target);
        this.annotations = annotations;
        this.position = position;
    }

    protected TypeSignatureNode(SignatureModel source, Schema<?> target,
            Integer position) {
        this(source, target, source.getAnnotations(), position);
    }

    public List<AnnotationInfoModel> getAnnotations() {
        return annotations;
    }

    public Integer getPosition() {
        return position;
    }

    public SignatureModel getType() {
        return getSource();
    }

    @Override
    public TypedNode processType(UnaryOperator<SignatureModel> typeProcessor) {
        var processedType = typeProcessor.apply(getSource());
        if (processedType.equals(getSource())) {
            return this;
        }

        return new TypeSignatureNode(typeProcessor.apply(getSource()),
                getTarget(), annotations, position);
    }

    @NonNull
    static public TypeSignatureNode of(@NonNull SignatureModel source) {
        return new TypeSignatureNode(source, new Schema<>(), null);
    }

    @NonNull
    static public TypeSignatureNode of(@NonNull SignatureModel source,
            int position) {
        return new TypeSignatureNode(source, new Schema<>(), position);
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
