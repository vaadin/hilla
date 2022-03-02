package dev.hilla.parser.plugins.model;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AssociationMap;
import dev.hilla.parser.models.SignatureModel;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.TypeSignature;
import io.swagger.v3.oas.models.media.Schema;

public final class ValidationConstraint {
    private final Map<String, Object> attributes;
    private final String simpleName;

    private ValidationConstraint(@Nonnull String simpleName,
            Map<String, Object> attributes) {
        this.simpleName = simpleName;
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public String getSimpleName() {
        return simpleName;
    }

    static class Processor {
        private static final String VALIDATION_CONSTRAINTS_KEY = "x-validation-constraints";
        private static final String VALIDATION_CONSTRAINTS_PACKAGE_NAME = "javax.validation.constraints";

        private final AssociationMap map;

        public Processor(@Nonnull AssociationMap map) {
            this.map = map;
        }

        public void process() {
            map.getTypes().forEach(this::processSchema);
        }

        private ValidationConstraint convertAnnotation(
                AnnotationInfo annotationInfo) {
            var simpleName = extractSimpleName(annotationInfo.getName());
            var attributes = annotationInfo.getParameterValues().stream()
                    .collect(Collectors.toMap(AnnotationParameterValue::getName,
                            AnnotationParameterValue::getValue));

            return new ValidationConstraint(simpleName,
                    attributes.size() > 0 ? attributes : null);
        }

        private String extractPackageName(String fullyQualifiedName) {
            return fullyQualifiedName.substring(0,
                    fullyQualifiedName.lastIndexOf("."));
        }

        private String extractSimpleName(String fullyQualifiedName) {
            return fullyQualifiedName
                    .substring(fullyQualifiedName.lastIndexOf(".") + 1);
        }

        private boolean isValidationConstraintAnnotation(
                AnnotationInfo annotationInfo) {
            return extractPackageName(annotationInfo.getName())
                    .equals(VALIDATION_CONSTRAINTS_PACKAGE_NAME);
        }

        private void processSchema(Schema<?> schema, SignatureModel signature) {
            if (!(signature.get() instanceof TypeSignature)) {
                return;
            }

            var annotationInfoList = ((TypeSignature) signature.get())
                    .getTypeAnnotationInfo();

            if (annotationInfoList != null) {
                var constraints = annotationInfoList.stream()
                        .filter(this::isValidationConstraintAnnotation)
                        .map(this::convertAnnotation)
                        .collect(Collectors.toList());

                if (!constraints.isEmpty()) {
                    schema.addExtension(VALIDATION_CONSTRAINTS_KEY,
                            constraints);
                }
            }
        }
    }
}
