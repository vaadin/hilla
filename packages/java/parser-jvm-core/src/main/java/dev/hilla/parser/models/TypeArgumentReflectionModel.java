package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.TypeArgument;

final class TypeArgumentReflectionModel extends AbstractModel<AnnotatedType>
        implements TypeArgumentModel, ReflectionSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<SignatureModel> associatedTypes;
    private TypeArgument.Wildcard wildcard;

    public TypeArgumentReflectionModel(AnnotatedType origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = Arrays.stream(origin.getAnnotations())
                .map(annotation -> AnnotationInfoModel.of(annotation, this))
                .collect(Collectors.toList());
        }

        return annotations;
    }

    @Override
    public List<SignatureModel> getAssociatedTypes() {
        if (associatedTypes == null) {
            var stream = origin instanceof AnnotatedWildcardType
                    ? StreamUtils.combine(
                            ((AnnotatedWildcardType) origin)
                                    .getAnnotatedLowerBounds(),
                            ((AnnotatedWildcardType) origin)
                                    .getAnnotatedUpperBounds())
                    : Stream.of(origin);

            associatedTypes = stream.map(type -> SignatureModel.of(type, this))
                    .distinct().collect(Collectors.toList());
        }

        return associatedTypes;
    }

    public TypeArgument.Wildcard getWildcard() {
        if (wildcard == null) {
            if (origin instanceof WildcardType) {
                var upperBounds = ((WildcardType) origin).getUpperBounds();

                if (((WildcardType) origin).getLowerBounds().length > 0) {
                    wildcard = TypeArgument.Wildcard.SUPER;
                } else if (!Objects.equals(upperBounds[0], Object.class)) {
                    wildcard = TypeArgument.Wildcard.EXTENDS;
                } else {
                    wildcard = TypeArgument.Wildcard.ANY;
                }
            } else {
                wildcard = TypeArgument.Wildcard.NONE;
            }
        }

        return wildcard;
    }
}
