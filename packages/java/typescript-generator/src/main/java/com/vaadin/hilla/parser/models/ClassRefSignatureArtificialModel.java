package com.vaadin.hilla.parser.models;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * An artificial ClassRefSignatureModel implementation.
 */
final class ClassRefSignatureArtificialModel extends ClassRefSignatureModel {
    private final List<AnnotationInfoModel> annotations;
    private final ClassInfoModel classInfo;
    private final List<TypeArgumentModel> typeArguments;

    ClassRefSignatureArtificialModel(@NonNull ClassInfoModel classInfo,
            @NonNull List<TypeArgumentModel> typeArguments,
            @NonNull List<AnnotationInfoModel> annotations) {
        this.classInfo = Objects.requireNonNull(classInfo);
        this.typeArguments = Objects.requireNonNull(typeArguments);
        this.annotations = Objects.requireNonNull(annotations);
    }

    @Override
    public Object get() {
        return null;
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
}
