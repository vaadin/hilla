package dev.hilla.parser.models;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.TypeArgument;

final class TypeArgumentReflectionModel extends
        AbstractReflectionSignatureDependable<WildcardType, Dependable<?, ?>>
        implements TypeArgumentModel, ReflectionSignatureModel {
    private TypeArgument.Wildcard wildcard;
    private Collection<TypeModel> wildcardAssociatedTypes;

    public TypeArgumentReflectionModel(WildcardType origin,
            Dependable<?, ?> parent) {
        super(origin, parent);
    }

    public TypeArgument.Wildcard getWildcard() {
        if (wildcard == null) {
            var upperBounds = origin.getUpperBounds();

            if (origin.getLowerBounds().length > 0) {
                wildcard = TypeArgument.Wildcard.SUPER;
            } else if (!Objects.equals(upperBounds[0], Object.class)) {
                wildcard = TypeArgument.Wildcard.EXTENDS;
            } else {
                wildcard = TypeArgument.Wildcard.ANY;
            }
        }

        return wildcard;
    }

    @Override
    public Collection<TypeModel> getWildcardAssociatedTypes() {
        if (wildcardAssociatedTypes == null) {
            wildcardAssociatedTypes = Stream
                    .of(Arrays.stream(origin.getLowerBounds()),
                            Arrays.stream(origin.getUpperBounds()))
                    .flatMap(Function.identity())
                    .map(type -> ReflectionSignatureModel.of(type, this))
                    .collect(Collectors.toSet());
        }

        return wildcardAssociatedTypes;
    }
}
