package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
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
        map.getSignatures().forEach(this::process);
    }

    private Stream<AnnotationInfoModel> getOwnerAnnotations(
            SignatureModel signature) {
        var info = map.reversed().getSignatureInfo().get(signature);

        if (info != null) {
            var base = info.getBase();

            if (base instanceof FieldInfoModel) {
                var field = (FieldInfoModel) base;
                return Stream.concat(
                        // If the signature is not nested, it has an effect
                        // of field annotations
                        field.getType().equals(signature)
                                ? field.getAnnotationsStream()
                                : Stream.empty(),
                        field.getOwner().getPackage().getAnnotationsStream());
            } else if (base instanceof MethodInfoModel) {
                var method = (MethodInfoModel) base;
                return Stream.concat(
                        // If the signature is not nested, it has an effect
                        // of method annotations
                        method.getResultType().equals(signature)
                                ? method.getAnnotationsStream()
                                : Stream.empty(),
                        method.getOwner().getPackage().getAnnotationsStream());
            } else if (base instanceof MethodParameterInfoModel) {
                var parameter = (MethodParameterInfoModel) base;
                return Stream.concat(
                        // If the signature is not nested, it has an effect
                        // of parameter annotations
                        parameter.getType().equals(signature)
                                ? parameter.getAnnotationsStream()
                                : Stream.empty(),
                        parameter.getOwner().getOwner().getPackage()
                                .getAnnotationsStream());
            }
        }

        return Stream.empty();
    }

    private void process(Schema<?> schema, SignatureModel signature) {
        var matcher = Stream
                .concat(signature.getAnnotationsStream(),
                        getOwnerAnnotations(signature))
                .map(annotation -> annotations.get(annotation.getName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(AnnotationMatcher::getScore))
                .orElse(AnnotationMatcher.DEFAULT);

        schema.setNullable(matcher.doesMakeNonNull() ? null : true);
    }
}
