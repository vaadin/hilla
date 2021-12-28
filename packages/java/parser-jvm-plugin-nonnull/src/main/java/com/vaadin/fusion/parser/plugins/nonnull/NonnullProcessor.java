package com.vaadin.fusion.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Objects;

import com.vaadin.fusion.parser.core.RelativeMethodInfo;
import com.vaadin.fusion.parser.core.RelativeMethodParameterInfo;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;
import com.vaadin.fusion.parser.plugins.backbone.AssociationMap;

import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeSignature;
import io.swagger.v3.oas.models.media.Schema;

final class NonnullProcessor {
    private final Collection<String> annotations;
    private final AssociationMap map;

    public NonnullProcessor(Collection<String> annotations,
            AssociationMap map) {
        this.annotations = annotations;
        this.map = map;
    }

    public void process() {
        map.getMethods().forEach(this::processMethod);
        map.getParameters().forEach(this::processParameter);
        map.getTypes().forEach(this::processSchema);
    }

    private boolean isNonNull(AnnotationInfoList annotationInfos) {
        return annotationInfos != null && annotationInfos.stream().anyMatch(
                annotation -> annotations.contains(annotation.getName()));
    }

    private boolean isNonNull(RelativeMethodInfo method) {
        return isNonNull(method.get().getAnnotationInfo());
    }

    private boolean isNonNull(RelativeMethodParameterInfo parameter) {
        return isNonNull(parameter.get().getAnnotationInfo());
    }

    private boolean isNonNull(RelativeTypeSignature signature) {
        AnnotationInfoList infos = null;

        var origin = signature.get();

        if (origin instanceof TypeSignature) {
            infos = ((TypeSignature) signature.get()).getTypeAnnotationInfo();
        } else if (origin instanceof TypeArgument) {
            infos = ((TypeArgument) origin).getTypeSignature()
                    .getTypeAnnotationInfo();
        }

        return isNonNull(infos);
    }

    private void processMethod(Schema<?> schema, RelativeMethodInfo method) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(method)) {
            schema.setNullable(null);
        }
    }

    private void processParameter(Schema<?> schema,
            RelativeMethodParameterInfo parameter) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(parameter)) {
            schema.setNullable(null);
        }
    }

    private void processSchema(Schema<?> schema,
            RelativeTypeSignature signature) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(signature)) {
            schema.setNullable(null);
        }
    }
}
