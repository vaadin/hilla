package dev.hilla.parser.models;

import java.util.Collection;
import java.util.stream.Collectors;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.ClassRefTypeSignature;

final class ClassRefSignatureSourceModel
        extends AbstractModel<ClassRefTypeSignature>
        implements ClassRefSignatureModel, SourceSignatureModel {
    private final ClassRefSignatureReflectionModel reflected;
    private Collection<TypeArgumentModel> typeArguments;

    public ClassRefSignatureSourceModel(ClassRefTypeSignature origin,
            Model parent) {
        super(origin, parent);
        reflected = new ClassRefSignatureReflectionModel(origin.loadClass());
    }

    @Override
    public Collection<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = StreamUtils
                    .combine(origin.getTypeArguments().stream(),
                            origin.getSuffixTypeArguments().stream()
                                    .flatMap(Collection::stream))
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
    public boolean isCharacter() {
        return reflected.isCharacter();
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
    public boolean isJDKClass() {
        return origin.getClassInfo() == null || reflected.isJDKClass();
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
    public boolean isString() {
        return reflected.isString();
    }
}
