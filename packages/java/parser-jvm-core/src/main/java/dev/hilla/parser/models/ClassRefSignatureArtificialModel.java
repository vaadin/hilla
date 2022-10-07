package dev.hilla.parser.models;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An artificial ClassRefSignatureModel implementation.
 */
final class ClassRefSignatureArtificialModel extends ClassRefSignatureModel {
    private final ClassInfoModel classInfo;

    private final List<TypeArgumentModel> typeArguments;

    private final List<AnnotationInfoModel> annotations;

    ClassRefSignatureArtificialModel(@Nonnull ClassInfoModel classInfo,
            @Nonnull List<TypeArgumentModel> typeArguments,
            @Nonnull List<AnnotationInfoModel> annotations) {
        this.classInfo = Objects.requireNonNull(classInfo);
        this.typeArguments = Objects.requireNonNull(typeArguments);
        this.annotations = Objects.requireNonNull(annotations);
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return annotations;
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return classInfo;
    }

    @Override
    protected Optional<ClassRefSignatureModel> prepareOwner() {
        return Optional.empty();
    }

    @Override
    protected List<TypeArgumentModel> prepareTypeArguments() {
        return typeArguments;
    }

    @Override
    public Object get() {
        return null;
    }
}
