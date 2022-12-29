package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.TypeArgument;

final class TypeArgumentReflectionModel extends TypeArgumentModel
        implements ReflectionSignatureModel {
    private final AnnotatedType origin;
    private TypeArgument.Wildcard wildcard;

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
    public TypeArgument.Wildcard getWildcard() {
        if (wildcard == null) {
            if (origin instanceof AnnotatedWildcardType) {
                var specific = (AnnotatedWildcardType) origin;

                if (specific.getAnnotatedLowerBounds().length > 0) {
                    wildcard = TypeArgument.Wildcard.SUPER;
                } else if (!specific.getAnnotatedUpperBounds()[0].getType()
                        .equals(Object.class)) {
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
