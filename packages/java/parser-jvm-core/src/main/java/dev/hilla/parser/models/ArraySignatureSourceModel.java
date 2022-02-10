package dev.hilla.parser.models;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassInfo;

final class ArraySignatureSourceModel
        extends AbstractSourceSignatureDependable<ArrayTypeSignature, Dependable<?, ?>>
        implements ArraySignatureModel, SourceSignatureModel {
    private TypeModel nestedType;

    public ArraySignatureSourceModel(ArrayTypeSignature origin, Dependable<?, ?> parent) {
        super(origin, parent);
    }

    @Override
    public TypeModel getNestedType() {
        if (nestedType == null) {
            nestedType = SourceSignatureModel.of(origin.getNestedType(), this);
        }

        return nestedType;
    }
}
