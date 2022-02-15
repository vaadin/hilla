package dev.hilla.parser.models;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.TypeArgument;

final class TypeArgumentReflectionModel
        extends AbstractReflectionSignatureModel<Type>
        implements TypeArgumentModel, ReflectionModel {
    private Collection<SignatureModel> associatedTypes;
    private TypeArgument.Wildcard wildcard;

    public TypeArgumentReflectionModel(Type origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public Collection<SignatureModel> getAssociatedTypes() {
        if (associatedTypes == null) {
            var stream = origin instanceof WildcardType ? Stream
                    .of(Arrays.stream(((WildcardType) origin).getLowerBounds()),
                            Arrays.stream(
                                    ((WildcardType) origin).getUpperBounds()))
                    .flatMap(Function.identity()) : Stream.of(origin);

            associatedTypes = stream
                    .map(type -> SignatureModel.of(type, this))
                    .collect(Collectors.toSet());
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
