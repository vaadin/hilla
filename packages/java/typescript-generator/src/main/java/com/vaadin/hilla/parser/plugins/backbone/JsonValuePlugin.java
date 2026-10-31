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
package com.vaadin.hilla.parser.plugins.backbone;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonValue;
import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.parser.utils.Signatures;

/**
 * Adds support for Jackson's {@code JsonValue} annotation.
 */
public class JsonValuePlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    private final Map<Class<?>, Optional<Class<?>>> jsonValues = new HashMap<>();

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    @NonNull
    @Override
    public Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> parentPath) {
        if (node instanceof TypedNode typedNode
                && Signatures.isClassRef(typedNode.getType())) {
            var cls = Signatures.getClass(typedNode.getType()).orElseThrow();
            // Check if the class has the annotations which qualify for a
            // value type. If so, replace the type with the corresponding
            // value type, in the same place among the types of its parent.
            var position = node instanceof TypeSignatureNode signatureNode
                    ? signatureNode.getPosition()
                    : null;
            Optional<Node<?, ?>> valueNode = getValueType(cls)
                    .map(Signatures::of)
                    .map(type -> position != null
                            ? TypeSignatureNode.of(type, position)
                            : TypeSignatureNode.of(type));
            return valueNode.orElse(node);
        }

        return node;
    }

    private Optional<Class<?>> getValueType(Class<?> cls) {
        // Use cached results to avoid recomputing the value type.
        return jsonValues.computeIfAbsent(cls, this::findValueType);
    }

    private Optional<Class<?>> findValueType(Class<?> cls) {
        for (var method : cls.getMethods()) {
            if (method.isAnnotationPresent(JsonValue.class)) {
                return Optional.of(method.getReturnType());
            }
        }

        return Optional.empty();
    }
}
