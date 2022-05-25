package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.TypeArgument;

final class TypeArgumentReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedType>
        implements TypeArgumentModel, ReflectionSignatureModel {
    private List<SignatureModel> associatedTypes;
    private TypeArgument.Wildcard wildcard;

    public TypeArgumentReflectionModel(AnnotatedType origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeArgumentModel)) {
            return false;
        }

        var other = (TypeArgumentModel) obj;

        return getAnnotations().equals(other.getAnnotations())
                && getAssociatedTypes().equals(other.getAssociatedTypes())
                && getWildcard().equals(other.getWildcard());
    }

    @Override
    public List<SignatureModel> getAssociatedTypes() {
        if (associatedTypes == null) {
            var stream = origin instanceof AnnotatedWildcardType
                    ? Streams.combine(
                            ((AnnotatedWildcardType) origin)
                                    .getAnnotatedLowerBounds(),
                            ((AnnotatedWildcardType) origin)
                                    .getAnnotatedUpperBounds())
                    : Stream.of(origin);

            associatedTypes = stream.map(SignatureModel::of).distinct()
                    .collect(Collectors.toList());
        }

        return associatedTypes;
    }

    public TypeArgument.Wildcard getWildcard() {
        if (wildcard == null) {
            if (origin instanceof WildcardType) {
                var upperBounds = ((WildcardType) origin).getUpperBounds();

                if (((WildcardType) origin).getLowerBounds().length > 0) {
                    wildcard = TypeArgument.Wildcard.SUPER;
                } else if (!upperBounds[0].equals(Object.class)) {
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
    public int hashCode() {
        return getAssociatedTypes().hashCode() + 7 * getWildcard().hashCode();
    }
}
