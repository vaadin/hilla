package dev.hilla.parser.models;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import io.github.classgraph.TypeArgument;

/**
 * An artificial type argument implementation.
 */
final class TypeArgumentArtificialModel extends TypeArgumentModel {
    private final TypeArgument.Wildcard wildcard;

    private final List<SignatureModel> associatedTypes;

    private final List<AnnotationInfoModel> annotations;

    TypeArgumentArtificialModel(@Nonnull TypeArgument.Wildcard wildcard,
            @Nonnull List<SignatureModel> associatedTypes,
            @Nonnull List<AnnotationInfoModel> annotations) {
        this.wildcard = Objects.requireNonNull(wildcard);
        this.associatedTypes = Objects.requireNonNull(associatedTypes);
        this.annotations = Objects.requireNonNull(annotations);
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return annotations;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public TypeArgument.Wildcard getWildcard() {
        return wildcard;
    }

    @Override
    protected List<SignatureModel> prepareAssociatedTypes() {
        return associatedTypes;
    }
}
