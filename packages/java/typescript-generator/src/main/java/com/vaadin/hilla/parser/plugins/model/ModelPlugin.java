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
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.AnnotatedModel;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.AnnotationParameterModel;
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

        var attributes = annotation.getParameters().stream()
                .filter(Predicate.not(AnnotationParameterModel::isDefault))
                .collect(Collectors.toMap(AnnotationParameterModel::getName,
                        AnnotationParameterModel::getValue));

        return new ValidationConstraint(simpleName,
                !attributes.isEmpty() ? attributes : null);
    }

    private static Annotation convertAnnotation(
            AnnotationInfoModel annotation) {
        return new Annotation(annotation.getName(), null);
    }

    private static String extractSimpleName(String fullyQualifiedName) {
        return fullyQualifiedName
                .substring(fullyQualifiedName.lastIndexOf(".") + 1);
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
        noteConstraints(typedNode, type);

        // The annotations of the property the value belongs to are told to the
        // form model of it as well
        if (nodePath.getParentPath() != null && nodePath.getParentPath()
                .getNode() instanceof PropertyNode propertyNode) {
            noteAnnotations(propertyNode.getSource(), type);
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {

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

    private void noteConstraints(AnnotatedNode annotatedNode, TypeFacts type) {
        var constraints = annotatedNode.getAnnotations().stream()
                .filter(ModelPlugin::isValidationConstraintAnnotation)
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
