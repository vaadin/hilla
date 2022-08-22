package dev.hilla.parser.plugins.model;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.AnnotationParameterModel;
import dev.hilla.parser.models.SignatureModel;
import dev.hilla.parser.plugins.backbone.AssociationMap;

final class ModelVisitor implements Visitor {
    private static final String VALIDATION_CONSTRAINTS_KEY = "x-validation-constraints";
    private static final String VALIDATION_CONSTRAINTS_PACKAGE_NAME = "javax.validation.constraints";
    private final AssociationMap associationMap;
    private final Supplier<Integer> orderProvider;
    private final int shift;

    ModelVisitor(AssociationMap associationMap, Supplier<Integer> orderProvider,
            int shift) {
        this.associationMap = associationMap;
        this.orderProvider = orderProvider;
        this.shift = shift;
    }

    private static ValidationConstraint convertAnnotation(
            AnnotationInfoModel annotation) {
        var simpleName = extractSimpleName(annotation.getName());

        var attributes = annotation.getParametersStream()
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
    public void enter(NodePath path) {
        if (path.isRemoved()) {
            return;
        }

        var model = path.getModel();

        if (model instanceof SignatureModel
                && associationMap.getSignatures().containsKey(model)) {
            var signature = (SignatureModel) model;

            if (!signature.isTypeArgument() && !signature.isTypeParameter()
                    && associationMap.getSignatures().containsKey(signature)) {
                var schema = associationMap.getSignatures().get(signature);

                var constraints = signature.getAnnotationsStream()
                        .filter(ModelVisitor::isValidationConstraintAnnotation)
                        .map(ModelVisitor::convertAnnotation)
                        .collect(Collectors.toList());

                if (!constraints.isEmpty()) {
                    schema.addExtension(VALIDATION_CONSTRAINTS_KEY,
                            constraints);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + shift;
    }
}
