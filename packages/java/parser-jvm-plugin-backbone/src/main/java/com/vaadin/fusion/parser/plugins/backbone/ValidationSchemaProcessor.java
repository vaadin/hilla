package com.vaadin.fusion.parser.plugins.backbone;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.RelativeTypeSignature;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.TypeSignature;
import io.swagger.v3.oas.models.media.Schema;

final class ValidationSchemaProcessor {
    static private final String VALIDATION_CONSTRAINTS_KEY = "x-validation-constraints";
    static private final String VALIDATION_CONSTRAINTS_PACKAGE_NAME = "javax.validation.constraints";
    private final Schema<?> schema;
    private final RelativeTypeSignature signature;

    ValidationSchemaProcessor(@Nonnull RelativeTypeSignature signature,
            @Nonnull Schema<?> schema) {
        this.signature = signature;
        this.schema = schema;
    }

    public void process() {
        if (!(signature.get() instanceof TypeSignature)) {
            return;
        }

        var annotationInfoList = ((TypeSignature) signature.get())
                .getTypeAnnotationInfo();
        if (annotationInfoList == null) {
            return;
        }

        List<ValidationConstraint> constraints = annotationInfoList.stream()
                .filter(this::isValidationConstraintAnnotation)
                .map(this::convertAnnotation).collect(Collectors.toList());
        if (constraints.isEmpty()) {
            return;
        }

        schema.addExtension(VALIDATION_CONSTRAINTS_KEY, constraints);
    }

    private ValidationConstraint convertAnnotation(
            AnnotationInfo annotationInfo) {
        return new ValidationConstraint.Builder()
                .withSimpleName(extractSimpleName(annotationInfo.getName()))
                .withAttributes(annotationInfo.getParameterValues().stream()
                        .collect(Collectors.toMap(
                                AnnotationParameterValue::getName,
                                AnnotationParameterValue::getValue)))
                .build();
    }

    private String extractPackageName(String fqn) {
        return fqn.substring(0, fqn.lastIndexOf("."));
    }

    private String extractSimpleName(String fqn) {
        return fqn.substring(fqn.lastIndexOf(".") + 1, fqn.length());
    }

    private boolean isValidationConstraintAnnotation(
            AnnotationInfo annotationInfo) {
        return extractPackageName(annotationInfo.getName())
                .equals(VALIDATION_CONSTRAINTS_PACKAGE_NAME);
    }
}
