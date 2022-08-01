package dev.hilla.parser.plugins.nonnull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.AssociationMap;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import dev.hilla.parser.models.SignatureModel;

import io.swagger.v3.oas.models.media.Schema;

final class NonnullProcessor {
    private final Map<String, AnnotationMatcher> annotations;
    private final AssociationMap map;

    public NonnullProcessor(Collection<AnnotationMatcher> annotations,
            AssociationMap map) {
        this.annotations = annotations.stream().collect(Collectors
                .toMap(AnnotationMatcher::getName, Function.identity()));
        this.map = map;
    }

    public void process() {
        map.getFields().forEach(this::processField);
        map.getMethods().forEach(this::processMethod);
        map.getParameters().forEach(this::processParameter);
        map.getTypes().forEach(this::processSchema);
    }

    private boolean isNonNull(Stream<AnnotationInfoModel> annotationsStream) {
        var x = annotationsStream
                .map(annotation -> annotations.get(annotation.getName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                .orElse(AnnotationMatcher.DEFAULT);
        return x.isNonNull();
    }

    private boolean isNonNull(FieldInfoModel field) {
        return isNonNull(Stream.concat(field.getAnnotationsStream(),
                field.getOwner().getPackage().getAnnotationsStream()));
    }

    private boolean isNonNull(MethodInfoModel method) {
        return isNonNull(Stream.concat(method.getAnnotationsStream(),
                method.getOwner().getPackage().getAnnotationsStream()));
    }

    private boolean isNonNull(MethodParameterInfoModel parameter) {
        return isNonNull(Stream.concat(parameter.getAnnotationsStream(),
                parameter.getOwner().getOwner().getPackage()
                        .getAnnotationsStream()));
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
