package com.vaadin.fusion.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Objects;

import com.vaadin.fusion.parser.core.AssociationMap;
import com.vaadin.fusion.parser.core.RelativeFieldInfo;
import com.vaadin.fusion.parser.core.RelativeMethodInfo;
import com.vaadin.fusion.parser.core.RelativeMethodParameterInfo;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;

import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.HierarchicalTypeSignature;
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
        map.getFields().forEach(this::processField);
        map.getMethods().forEach(this::processMethod);
        map.getParameters().forEach(this::processParameter);
        map.getTypes().forEach(this::processSchema);
    }

    private boolean isNonNull(AnnotationInfoList annotationInfos) {
        return annotationInfos != null && annotationInfos.stream().anyMatch(
                annotation -> annotations.contains(annotation.getName()));
    }

    private boolean isNonNull(RelativeFieldInfo field) {
        return isNonNull(field.get().getAnnotationInfo());
    }

    private boolean isNonNull(RelativeMethodInfo method) {
        return isNonNull(method.get().getAnnotationInfo());
    }

    private boolean isNonNull(RelativeMethodParameterInfo parameter) {
        return isNonNull(parameter.get().getAnnotationInfo());
    }

    private boolean isNonNull(HierarchicalTypeSignature signature) {
        if (signature instanceof ClassRefTypeSignature) {
            var infos = ((ClassRefTypeSignature) signature)
                    .getTypeAnnotationInfo();

            if (infos == null) {
                var suffixTypeAnnotations = ((ClassRefTypeSignature) signature)
                        .getSuffixTypeAnnotationInfo();

                // Having more than 1 suffix (like List<X.@X.B Y>) is some kind
                // of edge case for @Nonnull, so here we just get the
                // annotations for the latest one.
                infos = suffixTypeAnnotations != null
                        ? suffixTypeAnnotations
                                .get(suffixTypeAnnotations.size() - 1)
                        : null;
            }

            return isNonNull(infos);
        } else if (signature instanceof TypeSignature) {
            return isNonNull(
                    ((TypeSignature) signature).getTypeAnnotationInfo());
        } else if (signature instanceof TypeArgument) {
            return isNonNull(((TypeArgument) signature).getTypeSignature());
        }

        return false;
    }

    private void processField(Schema<?> schema, RelativeFieldInfo field) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(field)) {
            schema.setNullable(null);
        }
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
                && isNonNull(signature.get())) {
            schema.setNullable(null);
        }
    }
}
