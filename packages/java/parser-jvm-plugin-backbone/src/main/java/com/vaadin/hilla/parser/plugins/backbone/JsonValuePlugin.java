package com.vaadin.hilla.parser.plugins.backbone;

import com.fasterxml.jackson.annotation.JsonValue;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.models.*;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import org.jspecify.annotations.NonNull;

/**
 * Adds support for Jackson's {@code JsonValue} annotation.
 */
public class JsonValuePlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
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
                var valueType = getValueType(cls);
                return valueType == null ? node
                        : TypeSignatureNode.of(SignatureModel.of(valueType));
            }
        }

        return node;
    }

    private Class<?> getValueType(Class<?> cls) {
        for (var method : cls.getMethods()) {
            if (method.isAnnotationPresent(JsonValue.class)) {
                return method.getReturnType();
            }
        }

        return null;
    }
}
