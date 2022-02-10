package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

final class ClassRefSignatureSourceModel extends
        AbstractSourceSignatureDependable<ClassRefTypeSignature, Dependable<?, ?>>
        implements ClassRefSignatureModel, SourceSignatureModel {
    private final ClassRefSignatureReflectionModel reflected;
    private Collection<TypeArgumentModel> typeArguments;

    public ClassRefSignatureSourceModel(ClassRefTypeSignature origin,
            Dependable<?, ?> parent) {
        super(origin, parent);
        reflected = new ClassRefSignatureReflectionModel(origin.loadClass());
    }

    @Override
    public Collection<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = Stream
                    .of(origin.getTypeArguments().stream(),
                            origin.getSuffixTypeArguments().stream()
                                    .flatMap(Collection::stream))
                    .flatMap(Function.identity())
                    .map(arg -> TypeArgumentModel.of(arg, this))
                    .collect(Collectors.toList());
        }

        return typeArguments;
    }

    @Override
    public boolean isBoolean() {
        return reflected.isBoolean();
    }

    @Override
    public boolean isByte() {
        return reflected.isByte();
    }

    @Override
    public boolean isDate() {
        return reflected.isDate();
    }

    @Override
    public boolean isDateTime() {
        return reflected.isDateTime();
    }

    @Override
    public boolean isDouble() {
        return reflected.isDouble();
    }

    @Override
    public boolean isEnum() {
        return reflected.isEnum();
    }

    @Override
    public boolean isFloat() {
        return reflected.isFloat();
    }

    @Override
    public boolean isInteger() {
        return reflected.isInteger();
    }

    @Override
    public boolean isIterable() {
        return reflected.isIterable();
    }

    @Override
    public boolean isLong() {
        return reflected.isLong();
    }

    @Override
    public boolean isMap() {
        return reflected.isMap();
    }

    @Override
    public boolean isNativeObject() {
        return reflected.isNativeObject();
    }

    @Override
    public boolean isOptional() {
        return reflected.isOptional();
    }

    @Override
    public boolean isShort() {
        return reflected.isShort();
    }

    @Override
    public boolean isCharacter() {
        return reflected.isCharacter();
    }

    @Override
    public boolean isString() {
        return reflected.isString();
    }

    @Override
    public boolean isJDKClass() {
        return origin.getClassInfo() == null || reflected.isJDKClass();
    }
}
