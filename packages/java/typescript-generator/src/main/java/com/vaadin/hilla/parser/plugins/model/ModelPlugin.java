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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.AnnotatedModel;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.AnnotationParameterEnumValueModel;
import com.vaadin.hilla.parser.models.AnnotationParameterModel;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.Model;
import com.vaadin.hilla.parser.models.SignatureModel;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.TypeFacts;
import com.vaadin.hilla.parser.plugins.backbone.nodes.AnnotatedNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;

public final class ModelPlugin extends AbstractPlugin<PluginConfiguration> {
    private static final String VALIDATION_CONSTRAINTS_PACKAGE_NAME = "jakarta.validation.constraints";

    // Include-list of annotations that should be added to the schema
    private static final Set<String> INCLUDED_ANNOTATIONS = Set.of(
            "jakarta.persistence.Id", "jakarta.persistence.Version",
            "jakarta.persistence.OneToOne", "jakarta.persistence.ManyToOne",
            "jakarta.persistence.OneToMany", "jakarta.persistence.ManyToMany");

    public ModelPlugin() {
        super();
    }

    private static ValidationConstraint convertValidationConstraintAnnotation(
            AnnotationInfoModel annotation) {
        var simpleName = extractSimpleName(annotation.getName());

        return new ValidationConstraint(simpleName,
                extractAttributes(annotation));
    }

    private static Annotation convertAnnotation(
            AnnotationInfoModel annotation) {
        return new Annotation(annotation.getName(),
                extractAttributes(annotation));
    }

    private static Map<String, Object> extractAttributes(
            AnnotationInfoModel annotation) {
        // Ordered by name: the parameters of an annotation come in the order
        // reflection lists its methods in, which is no order at all
        var attributes = annotation.getParameters().stream()
                .filter(Predicate.not(AnnotationParameterModel::isDefault))
                .collect(Collectors.toMap(AnnotationParameterModel::getName,
                        parameter -> convertAttributeValue(
                                parameter.getValue()),
                        (first, second) -> first, TreeMap::new));

        return !attributes.isEmpty() ? attributes : null;
    }

    /**
     * Converts an annotation parameter value into something the OpenAPI
     * document can hold. The parser hands back its own models for enum
     * constants and class references. An array arrives as a list of such
     * values.
     */
    private static Object convertAttributeValue(Object value) {
        return switch (value) {
        case AnnotationParameterEnumValueModel enumValue ->
            enumValue.getValueName();
        case ClassInfoModel classInfo -> new JvmTypeRef(classInfo.getName());
        case Collection<?> collection -> collection.stream()
                .map(ModelPlugin::convertAttributeValue).toList();
        // Every other model carries a whole type graph behind it, which the
        // serializer would write into the document. The only kind left is a
        // nested annotation, which nothing reaching this plugin has: the
        // include-list holds none, and a repeated constraint is unwrapped
        // before it gets here. Adding one means adding its conversion too.
        case Model model -> throw new IllegalStateException(
                "No OpenAPI representation for the annotation attribute value "
                        + model);
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
    private static Stream<AnnotationInfoModel> unwrapRepeatableContainer(
            AnnotationInfoModel annotation) {
        var parameters = annotation.getParameters();

        if (parameters.size() == 1) {
            var parameter = parameters.iterator().next();

            if ("value".equals(parameter.getName())
                    && parameter.getValue() instanceof Collection<?> values
                    && !values.isEmpty() && values.stream()
                            .allMatch(AnnotationInfoModel.class::isInstance)) {
                return values.stream().map(AnnotationInfoModel.class::cast);
            }
        }

        return Stream.of(annotation);
    }

    private static boolean isValidationConstraintAnnotation(
            AnnotationInfoModel annotation) {
        return annotation.getName()
                .startsWith(VALIDATION_CONSTRAINTS_PACKAGE_NAME);
    }

    private static boolean isIncludedAnnotation(
            AnnotationInfoModel annotation) {
        return INCLUDED_ANNOTATIONS.contains(annotation.getName());
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (!(nodePath.getNode() instanceof TypedNode typedNode)) {
            return;
        }

        var signature = (SignatureModel) typedNode.getType();
        if (signature.isTypeArgument() || signature.isTypeParameter()) {
            return;
        }

        var type = typedNode.getTarget();
        noteConstraints(constrainingAnnotations(nodePath, typedNode), type);

        // The annotations of the property the value belongs to are told to the
        // form model of it as well
        if (nodePath.getParentPath() != null && nodePath.getParentPath()
                .getNode() instanceof PropertyNode propertyNode) {
            noteAnnotations(propertyNode.getSource(), type);
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
    private static List<AnnotationInfoModel> constrainingAnnotations(
            NodePath<?> nodePath, AnnotatedNode annotatedNode) {
        var annotations = annotatedNode.getAnnotations();
        var arrayProperty = findArrayProperty(nodePath);
        if (arrayProperty.isEmpty()) {
            return annotations;
        }

        var declared = arrayProperty.get().getSource().getAnnotations();
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
                && parent.getType().isArray()) {
            path = path.getParentPath();
        }

        if (path.getNode() instanceof TypedNode typedNode
                && typedNode.getType().isArray() && path.getParentPath() != null
                && path.getParentPath()
                        .getNode() instanceof PropertyNode propertyNode) {
            return Optional.of(propertyNode);
        }

        return Optional.empty();
    }

    private void noteConstraints(List<AnnotationInfoModel> annotations,
            TypeFacts type) {
        var constraints = annotations.stream()
                .filter(ModelPlugin::isValidationConstraintAnnotation)
                .flatMap(ModelPlugin::unwrapRepeatableContainer)
                .map(ModelPlugin::convertValidationConstraintAnnotation)
                .collect(Collectors.toList());

        if (!constraints.isEmpty()) {
            type.note(TypeFacts.CONSTRAINTS, constraints);
        }
    }

    private void noteAnnotations(AnnotatedModel annotatedModel,
            TypeFacts type) {
        var annotations = annotatedModel.getAnnotations().stream()
                .filter(ModelPlugin::isIncludedAnnotation)
                .map(ModelPlugin::convertAnnotation)
                .collect(Collectors.toList());

        if (!annotations.isEmpty()) {
            type.note(TypeFacts.ANNOTATIONS, annotations);
        }
    }

}
