package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import dev.hilla.parser.core.AssociationMap;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.SignatureModel;

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

    private boolean isNonNull(Stream<AnnotationInfoModel> annotationsStream) {
        return annotationsStream.anyMatch(
                annotation -> annotations.contains(annotation.getName()));
    }

    private boolean isNonNull(FieldInfoModel field) {
        return isNonNull(field.getAnnotationsStream());
    }

    private boolean isNonNull(MethodInfoModel method) {
        return isNonNull(method.getAnnotationsStream());
    }

    private boolean isNonNull(MethodParameterInfoModel parameter) {
        return isNonNull(parameter.getAnnotationsStream());
    }

    private boolean isNonNull(SignatureModel signature) {
        return isNonNull(signature.getAnnotationsStream());
    }

    private void processField(Schema<?> schema, FieldInfoModel field) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(field)) {
            schema.setNullable(null);
        }
    }

    private void processMethod(Schema<?> schema, MethodInfoModel method) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(method)) {
            schema.setNullable(null);
        }
    }

    private void processParameter(Schema<?> schema,
            MethodParameterInfoModel parameter) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(parameter)) {
            schema.setNullable(null);
        }
    }

    private void processSchema(Schema<?> schema, SignatureModel signature) {
        if (Objects.equals(schema.getNullable(), Boolean.TRUE)
                && isNonNull(signature)) {
            schema.setNullable(null);
        }
    }
}
