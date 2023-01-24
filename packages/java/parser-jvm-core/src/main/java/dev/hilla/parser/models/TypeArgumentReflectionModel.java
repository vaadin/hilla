package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.Streams;

final class TypeArgumentReflectionModel extends TypeArgumentModel
        implements ReflectionSignatureModel {
    private final AnnotatedType origin;
    private Wildcard wildcard;

    TypeArgumentReflectionModel(AnnotatedType origin) {
        this.origin = origin;
    }

    private static boolean isNonNativeObjectType(AnnotatedType type) {
        return type.getType() != Object.class;
    }

    @Override
    public AnnotatedType get() {
        return origin;
    }

    @Override
    public Wildcard getWildcard() {
        if (wildcard == null) {
            if (origin instanceof AnnotatedWildcardType) {
                var specific = (AnnotatedWildcardType) origin;

                if (specific.getAnnotatedLowerBounds().length > 0) {
                    wildcard = Wildcard.SUPER;
                } else if (!specific.getAnnotatedUpperBounds()[0].getType()
                        .equals(Object.class)) {
                    wildcard = Wildcard.EXTENDS;
                } else {
                    wildcard = Wildcard.ANY;
                }
            } else {
                wildcard = Wildcard.NONE;
            }
        }

        return wildcard;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected List<SignatureModel> prepareAssociatedTypes() {
        var stream = origin instanceof AnnotatedWildcardType ? Streams.combine(
                ((AnnotatedWildcardType) origin).getAnnotatedLowerBounds(),
                ((AnnotatedWildcardType) origin).getAnnotatedUpperBounds())
                : Stream.of(origin);

        return stream.filter(TypeArgumentReflectionModel::isNonNativeObjectType)
                .map(SignatureModel::of).distinct()
                .collect(Collectors.toList());
    }
}
