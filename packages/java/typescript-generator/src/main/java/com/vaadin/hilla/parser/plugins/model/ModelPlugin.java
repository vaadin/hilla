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
package com.vaadin.hilla.parser.plugins.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.generator.model.AnnotationModel;
import com.vaadin.hilla.generator.model.ConstraintModel;
import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.nodes.AnnotatedNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.parser.utils.Signatures;

public final class ModelPlugin extends AbstractPlugin<PluginConfiguration> {
    private static final String VALIDATION_CONSTRAINTS_PACKAGE_NAME = "jakarta.validation.constraints";

    // Include-list of annotations that should be added to the form model
    private static final Set<String> INCLUDED_ANNOTATIONS = Set.of(
            "jakarta.persistence.Id", "jakarta.persistence.Version",
            "jakarta.persistence.OneToOne", "jakarta.persistence.ManyToOne",
            "jakarta.persistence.OneToMany", "jakarta.persistence.ManyToMany");

    public ModelPlugin() {
        super();
    }

    private static ConstraintModel convertValidationConstraintAnnotation(
            java.lang.annotation.Annotation annotation) {
        var simpleName = extractSimpleName(
                annotation.annotationType().getName());

        return new ConstraintModel(simpleName, extractAttributes(annotation));
    }

    private static AnnotationModel convertAnnotation(
            java.lang.annotation.Annotation annotation) {
        return new AnnotationModel(annotation.annotationType().getName(),
                extractAttributes(annotation));
    }

    private static Map<String, Object> extractAttributes(
            java.lang.annotation.Annotation annotation) {
        // Ordered by name: the parameters of an annotation come in the order
        // reflection lists its methods in, which is no order at all
        return parametersOf(annotation).stream()
                .filter(Predicate.not(Parameter::isDefault))
                .collect(Collectors.toMap(Parameter::name,
                        parameter -> convertAttributeValue(parameter.value()),
                        (first, second) -> first, TreeMap::new));
    }

    /**
     * Reads the parameters of an annotation, telling which of them keep their
     * default value.
     */
    private static List<Parameter> parametersOf(
            java.lang.annotation.Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .map(method -> {
                    // An annotation method is public and takes no arguments,
                    // so invoking it gives the value of the parameter
                    try {
                        var value = method.invoke(annotation);
                        return new Parameter(method.getName(), value, Objects
                                .deepEquals(method.getDefaultValue(), value));
                    } catch (InvocationTargetException
                            | IllegalAccessException e) {
                        throw new IllegalStateException(
                                "Unable to read the parameter "
                                        + method.getName() + " of "
                                        + annotation,
                                e);
                    }
                }).toList();
    }

    /**
     * Converts an annotation parameter value into something the form model
     * writer can write as TypeScript, so that it only meets strings, numbers,
     * booleans, lists and maps: an enum constant becomes its name, a class
     * becomes the map naming it which the model library reads, and an array of
     * objects becomes a list of such values.
     */
    private static Object convertAttributeValue(Object value) {
        return switch (value) {
        case Enum<?> enumValue -> enumValue.name();
        case Class<?> cls -> Map.of("jvmType", cls.getName());
        case Object[] array -> Arrays.stream(array)
                .map(ModelPlugin::convertAttributeValue).toList();
        // A nested annotation has no TypeScript value to be written as, and
        // nothing reaching this plugin has one: the include-list holds none,
        // and a repeated constraint is unwrapped before it gets here. Adding
        // one means adding its conversion too.
        case java.lang.annotation.Annotation annotation ->
            throw new IllegalStateException(
                    "No TypeScript representation for the annotation attribute value "
                            + annotation);
        default -> value;
        };
    }

    private static String extractSimpleName(String fullyQualifiedName) {
        return fullyQualifiedName
                .substring(fullyQualifiedName.lastIndexOf(".") + 1);
    }

    /**
     * Replaces the container the compiler synthesizes for a repeated
     * constraint, such as {@code @Pattern.List} for two {@code @Pattern}s, with
     * the constraints it holds. The container has no validator of its own, so
     * emitting it would ask the form binder for a name it does not export.
     * <p>
     * A container is recognized by its shape rather than by its name: a single
     * {@code value} parameter holding the repeated annotations.
     */
    private static Stream<java.lang.annotation.Annotation> unwrapRepeatableContainer(
            java.lang.annotation.Annotation annotation) {
        var parameters = parametersOf(annotation);

        if (parameters.size() == 1) {
            var parameter = parameters.get(0);

            if ("value".equals(parameter.name()) && parameter
                    .value() instanceof java.lang.annotation.Annotation[] values
                    && values.length > 0) {
                return Arrays.stream(values);
            }
        }

        return Stream.of(annotation);
    }

    private static boolean isValidationConstraintAnnotation(
            java.lang.annotation.Annotation annotation) {
        return annotation.annotationType().getName()
                .startsWith(VALIDATION_CONSTRAINTS_PACKAGE_NAME);
    }

    private static boolean isIncludedAnnotation(
            java.lang.annotation.Annotation annotation) {
        return INCLUDED_ANNOTATIONS
                .contains(annotation.annotationType().getName());
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (!(nodePath.getNode() instanceof TypedNode typedNode)) {
            return;
        }

        var signature = typedNode.getType();
        if (Signatures.isTypeArgument(signature)
                || Signatures.isTypeParameter(signature)) {
            return;
        }

        var type = typedNode.getTarget();
        noteConstraints(constrainingAnnotations(nodePath, typedNode), type);

        // The annotations of the property the value belongs to are told to the
        // form model of it as well
        if (nodePath.getParentPath() != null && nodePath.getParentPath()
                .getNode() instanceof PropertyNode propertyNode) {
            noteAnnotations(propertyNode, type);
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    /**
     * The annotations a value is constrained by. Java reads an annotation
     * written in front of an array property as one of what the array holds as
     * well, but Bean Validation checks the array itself with it, so the array
     * gets the annotations of the property instead of the values in it.
     */
    private static List<java.lang.annotation.Annotation> constrainingAnnotations(
            NodePath<?> nodePath, AnnotatedNode annotatedNode) {
        var annotations = annotatedNode.getAnnotations();
        var arrayProperty = findArrayProperty(nodePath);
        if (arrayProperty.isEmpty()) {
            return annotations;
        }

        var declared = arrayProperty.get().getAnnotations();
        if (nodePath.getParentPath().getNode() == arrayProperty.get()) {
            return Stream.concat(annotations.stream(), declared.stream())
                    .distinct().collect(Collectors.toList());
        }

        return annotations.stream().filter(Predicate.not(declared::contains))
                .collect(Collectors.toList());
    }

    /**
     * The property whose value is the array the node is, or is in.
     */
    private static Optional<PropertyNode> findArrayProperty(
            NodePath<?> nodePath) {
        var path = nodePath;
        while (path.getParentPath() != null
                && path.getParentPath().getNode() instanceof TypedNode parent
                && Signatures.isArray(parent.getType())) {
            path = path.getParentPath();
        }

        if (path.getNode() instanceof TypedNode typedNode
                && Signatures.isArray(typedNode.getType())
                && path.getParentPath() != null && path.getParentPath()
                        .getNode() instanceof PropertyNode propertyNode) {
            return Optional.of(propertyNode);
        }

        return Optional.empty();
    }

    private void noteConstraints(
            List<java.lang.annotation.Annotation> annotations,
            TypeModel.Builder type) {
        type.setConstraints(annotations.stream()
                .filter(ModelPlugin::isValidationConstraintAnnotation)
                .flatMap(ModelPlugin::unwrapRepeatableContainer)
                .map(ModelPlugin::convertValidationConstraintAnnotation)
                .toList());
    }

    private void noteAnnotations(AnnotatedNode annotatedNode,
            TypeModel.Builder type) {
        type.setAnnotations(annotatedNode.getAnnotations().stream()
                .filter(ModelPlugin::isIncludedAnnotation)
                .map(ModelPlugin::convertAnnotation).toList());
    }

    private record Parameter(String name, Object value, boolean isDefault) {
    }
}
