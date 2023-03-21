package dev.hilla.parser.plugins.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.AnnotationParameterModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.nodes.TypeSignatureNode;

import io.swagger.v3.oas.models.media.Schema;

public final class ModelPlugin extends AbstractPlugin<PluginConfiguration> {
    private static final String VALIDATION_CONSTRAINTS_KEY = "x-validation-constraints";
    private static final String VALIDATION_CONSTRAINTS_PACKAGE_NAME = "jakarta.validation.constraints";

    public ModelPlugin() {
        super();
    }

    private static ValidationConstraint convertAnnotation(
            AnnotationInfoModel annotation) {
        var simpleName = extractSimpleName(annotation.getName());

        var attributes = annotation.getParameters().stream()
                .filter(Predicate.not(AnnotationParameterModel::isDefault))
                .collect(Collectors.toMap(AnnotationParameterModel::getName,
                        AnnotationParameterModel::getValue));

        return new ValidationConstraint(simpleName,
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

    @Override
    public void enter(NodePath<?> nodePath) {
        if (!(nodePath.getNode() instanceof TypeSignatureNode)) {
            return;
        }

        var signatureNode = (TypeSignatureNode) nodePath.getNode();
        var signature = (SignatureModel) signatureNode.getSource();
        if (signature.isTypeArgument() || signature.isTypeParameter()) {
            return;
        }

        var schema = signatureNode.getTarget();
        addConstraintsToSchema(signature, schema);
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

    private void addConstraintsToSchema(SignatureModel signature,
            Schema<?> schema) {
        var constraints = signature.getAnnotations().stream()
                .filter(ModelPlugin::isValidationConstraintAnnotation)
                .map(ModelPlugin::convertAnnotation)
                .collect(Collectors.toList());

        if (!constraints.isEmpty()) {
            schema.addExtension(VALIDATION_CONSTRAINTS_KEY, constraints);
        }
    }
}
