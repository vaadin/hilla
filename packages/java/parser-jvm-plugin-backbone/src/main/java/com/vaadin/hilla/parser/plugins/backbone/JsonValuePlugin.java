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

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Adds support for Jackson's {@code JsonValue} and {@code JsonCreator}
 * annotations.
 */
public class JsonValuePlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
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
                // First of all, we check that the `@JsonValue` annotation is
                // used on a method of the class.
                var jsonValue = classRefSignatureModel.getClassInfo()
                        .getMethods().stream()
                        .filter(method -> method.getAnnotations().stream()
                                .map(NamedModel::getName)
                                .anyMatch(name -> name
                                        .equals(JsonValue.class.getName())))
                        .map(MethodInfoModel::getResultType).findFirst();

                if (jsonValue.isPresent()) {
                    // Then we check that the class has a `@JsonCreator`
                    // annotation on a method or on a constructor.
                    // This is a basic check, we could also check that they use
                    // the same type, or if a class uses `@JsonCreator` without
                    // `@JsonValue`.
                    var cls = (Class<?>) classRefSignatureModel.getClassInfo()
                            .get();
                    Stream.concat(Arrays.stream(cls.getMethods()),
                            Arrays.stream(cls.getConstructors()))
                            .filter(executable -> executable
                                    .isAnnotationPresent(JsonCreator.class))
                            .findAny().orElseThrow(
                                    () -> new MissingJsonCreatorAnnotationException(
                                            "Class " + cls.getName()
                                                    + " has @JsonValue, but no @JsonCreator."
                                                    + " Hilla only supports classes with both annotations."));
                }

                // If the class has both annotations, we return the return type
                // of the `@JsonValue`-annotated method, otherwise we return the
                // type itself as usual.
                return jsonValue.map(TypeSignatureNode::of)
                        .orElse(typeSignatureNode);
            }
        }

        return node;
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
