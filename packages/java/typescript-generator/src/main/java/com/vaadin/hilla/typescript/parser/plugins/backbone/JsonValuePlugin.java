package com.vaadin.hilla.typescript.parser.plugins.backbone;

import com.fasterxml.jackson.annotation.JsonValue;
import com.vaadin.hilla.typescript.parser.core.AbstractPlugin;
import com.vaadin.hilla.typescript.parser.core.Node;
import com.vaadin.hilla.typescript.parser.core.NodeDependencies;
import com.vaadin.hilla.typescript.parser.core.NodePath;
import com.vaadin.hilla.typescript.parser.models.*;
import com.vaadin.hilla.typescript.parser.plugins.backbone.nodes.TypeSignatureNode;
import com.vaadin.hilla.typescript.parser.plugins.backbone.nodes.TypedNode;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adds support for Jackson's {@code JsonValue} annotation.
 */
public class JsonValuePlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    private final Map<Class<?>, Optional<Class<?>>> jsonValues = new HashMap<>();

    @Override
    public void enter(NodePath<?> nodePath) {
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    @NonNull
    @Override
    public Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> parentPath) {
        if (node instanceof TypedNode typedNode) {
            if (typedNode
                    .getType() instanceof ClassRefSignatureModel classRefSignatureModel) {
                var cls = (Class<?>) classRefSignatureModel.getClassInfo()
                        .get();
                // Check if the class has the annotations which qualify for a
                // value type. If so, replace the type with the corresponding
                // value type.
                Optional<Node<?, ?>> valueNode = getValueType(cls)
                        .map(SignatureModel::of).map(TypeSignatureNode::of);
                return valueNode.orElse(node);
            }
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
