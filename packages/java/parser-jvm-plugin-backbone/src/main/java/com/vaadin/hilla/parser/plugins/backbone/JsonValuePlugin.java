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
    private static class NotAJsonValue {
    }

    private final Map<Class<?>, Class<?>> jsonValues = new HashMap<>();

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
                Optional<TypeSignatureNode> valueNode = getValueType(cls)
                        .map(SignatureModel::of).map(TypeSignatureNode::of);
                return valueNode.orElse(typeSignatureNode);
            }
        }

        return node;
    }

    private Optional<Class<?>> getValueType(Class<?> cls) {
        // Use cached results to avoid recomputing the value type.
        var valueType = jsonValues.computeIfAbsent(cls, this::findValueType);
        return valueType == NotAJsonValue.class ? Optional.empty()
                : Optional.of(valueType);
    }

    private Class<?> findValueType(Class<?> cls) {
        // First of all, we check that the `@JsonValue` annotation is
        // used on a method of the class.
        var jsonValue = Arrays.stream(cls.getMethods())
                .filter(method -> Arrays.stream(method.getAnnotations())
                        .map(ann -> ann.annotationType().getName()).anyMatch(
                                name -> name.equals(JsonValue.class.getName())))
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
            throw new MissingJsonCreatorAnnotationException("Class "
                    + cls.getName() + " has @JsonValue, but no @JsonCreator."
                    + " Hilla only supports classes with both annotations.");
        }

        return jsonValue.orElse(NotAJsonValue.class);
    }

    // this shouldn't be a runtime exception, but `resolve` doesn't allow
    // checked exceptions
    public static class MissingJsonCreatorAnnotationException
            extends RuntimeException {
        public MissingJsonCreatorAnnotationException(String message) {
            super(message);
        }
    }
}
