package com.vaadin.hilla.parser.plugins.backbone;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.models.*;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;
import jakarta.annotation.Nonnull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Adds support for Jackson's {@code JsonValue} and {@code JsonCreator}
 * annotations.
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

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    @Nonnull
    @Override
    public Node<?, ?> resolve(@Nonnull Node<?, ?> node,
            @Nonnull NodePath<?> parentPath) {
        if (node instanceof TypeSignatureNode typeSignatureNode) {
            if (typeSignatureNode
                    .getSource() instanceof ClassRefSignatureModel classRefSignatureModel) {
                var cls = (Class<?>) classRefSignatureModel.getClassInfo()
                        .get();
                // Check if the class has the annotations which qualify for a
                // value type. If so, replace the type with the corresponding
                // value type.
                Optional<TypeSignatureNode> valueNode = getValueType(cls)
                        .map(SignatureModel::of).map(TypeSignatureNode::of);
                return valueNode.orElse(typeSignatureNode);
            }
        }

        return node;
    }

    private Optional<Class<?>> getValueType(Class<?> cls) {
        // Use cached results to avoid recomputing the value type.
        return jsonValues.computeIfAbsent(cls, this::findValueType);
    }

    private Optional<Class<?>> findValueType(Class<?> cls) {
        // First of all, we check that the `@JsonValue` annotation is
        // used on a method of the class.
        var jsonValue = Arrays.stream(cls.getMethods())
                .filter(method -> method.isAnnotationPresent(JsonValue.class))
                .map(Method::getReturnType).findAny();

        // Then we check that the class has a `@JsonCreator` annotation
        // on a method or on a constructor. This is a basic check, we
        // could also check that they use the same type.
        var jsonCreator = Stream
                .concat(Arrays.stream(cls.getMethods()),
                        Arrays.stream(cls.getConstructors()))
                .filter(executable -> executable
                        .isAnnotationPresent(JsonCreator.class))
                .findAny();

        // Classes having only one of those annotation are malformed in Hilla as
        // they break the generator or, at least, make data transfer impossible,
        // so we throw an exception for those.
        if (jsonValue.isPresent() ^ jsonCreator.isPresent()) {
            throw new MalformedValueTypeException("Class " + cls.getName()
                    + " has only one of @JsonValue and @JsonCreator."
                    + " Hilla only supports classes with both annotations.");
        }

        return jsonValue;
    }

    // this shouldn't be a runtime exception, but `resolve` doesn't allow
    // checked exceptions
    public static class MalformedValueTypeException extends RuntimeException {
        public MalformedValueTypeException(String message) {
            super(message);
        }
    }
}
