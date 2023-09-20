package dev.hilla.parser.plugins.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.AnnotatedModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.AnnotationParameterModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.nodes.AnnotatedNode;
import dev.hilla.parser.plugins.backbone.nodes.PropertyNode;
import dev.hilla.parser.plugins.backbone.nodes.TypedNode;

import io.swagger.v3.oas.models.media.Schema;

public final class ModelPlugin extends AbstractPlugin<PluginConfiguration> {
    private static final String VALIDATION_CONSTRAINTS_KEY = "x-validation-constraints";
    private static final String ANNOTATIONS_KEY = "x-annotations";
    private static final String VALIDATION_CONSTRAINTS_PACKAGE_NAME = "jakarta.validation.constraints";

    // Include-list of annotations that should be added to the schema
    private static final Set<String> INCLUDED_ANNOTATIONS = Set
            .of("jakarta.persistence.Id", "jakarta.persistence.Version");

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
        var attributes = annotation.getParameters().stream()
                .filter(Predicate.not(AnnotationParameterModel::isDefault))
                .collect(Collectors.toMap(AnnotationParameterModel::getName,
                        AnnotationParameterModel::getValue));

        return new Annotation(annotation.getName(),
                !attributes.isEmpty() ? attributes : null);
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

        var schema = typedNode.getTarget();
        addConstraintsToSchema(typedNode, schema);

        // Add annotations from parent property model to schema
        if (nodePath.getParentPath() != null && nodePath.getParentPath()
                .getNode() instanceof PropertyNode propertyNode) {
            var propertyModel = propertyNode.getSource();
            addAnnotationsToSchema(propertyModel, schema);
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {

    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

    private void addConstraintsToSchema(AnnotatedNode annotatedNode,
            Schema<?> schema) {
        var constraints = annotatedNode.getAnnotations().stream()
                .filter(ModelPlugin::isValidationConstraintAnnotation)
                .map(ModelPlugin::convertValidationConstraintAnnotation)
                .collect(Collectors.toList());

        if (!constraints.isEmpty()) {
            schema.addExtension(VALIDATION_CONSTRAINTS_KEY, constraints);
        }
    }

    private void addAnnotationsToSchema(AnnotatedModel annotatedModel,
            Schema<?> schema) {
        var annotations = annotatedModel.getAnnotations().stream()
                .filter(ModelPlugin::isIncludedAnnotation)
                .map(ModelPlugin::convertAnnotation)
                .collect(Collectors.toList());

        if (!annotations.isEmpty()) {
            schema.addExtension(ANNOTATIONS_KEY, annotations);
        }
    }
}
