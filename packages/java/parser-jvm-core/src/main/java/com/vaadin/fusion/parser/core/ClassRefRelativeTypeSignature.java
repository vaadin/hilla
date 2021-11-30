package com.vaadin.fusion.parser.core;

import static com.vaadin.fusion.parser.core.ParserUtils.isJDKClass;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

public final class ClassRefRelativeTypeSignature
        extends AbstractRelative<ClassRefTypeSignature, Relative<?>>
        implements RelativeTypeSignature {
    private final ReflectedClass reflectedClass;
    private final List<List<RelativeTypeArgument>> suffixTypeArguments;
    private final List<RelativeTypeArgument> typeArguments;

    ClassRefRelativeTypeSignature(ClassRefTypeSignature origin,
            Relative<?> parent) {
        super(origin, parent);
        reflectedClass = new ReflectedClass(origin);
        typeArguments = origin.getTypeArguments().stream()
                .map(arg -> new RelativeTypeArgument(arg, this))
                .collect(Collectors.toList());
        suffixTypeArguments = origin.getSuffixTypeArguments().stream()
                .map(list -> list.stream()
                        .map(arg -> new RelativeTypeArgument(arg, this))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static Stream<ClassInfo> resolve(
            @Nonnull ClassRefTypeSignature signature) {
        var classInfo = Objects.requireNonNull(signature).getClassInfo();

        var typeArgumentsDependencies = signature.getTypeArguments().stream()
                .flatMap(argument -> RelativeTypeSignature
                        .resolve(argument.getTypeSignature()))
                .distinct();

        return classInfo != null && !isJDKClass(classInfo.getName())
                ? Stream.of(Stream.of(classInfo), typeArgumentsDependencies)
                        .flatMap(Function.identity()).distinct()
                : typeArgumentsDependencies;
    }

    public List<List<RelativeTypeArgument>> getSuffixTypeArguments() {
        return suffixTypeArguments;
    }

    public List<RelativeTypeArgument> getTypeArguments() {
        return typeArguments;
    }

    @Override
    public boolean isBoolean() {
        return reflectedClass.isBoolean();
    }

    @Override
    public boolean isByte() {
        return reflectedClass.isByte();
    }

    @Override
    public boolean isClassRef() {
        return true;
    }

    @Override
    public boolean isDate() {
        return reflectedClass.isDate();
    }

    @Override
    public boolean isDateTime() {
        return reflectedClass.isDateTime();
    }

    @Override
    public boolean isDouble() {
        return reflectedClass.isDouble();
    }

    @Override
    public boolean isEnum() {
        return reflectedClass.isEnum();
    }

    @Override
    public boolean isFloat() {
        return reflectedClass.isFloat();
    }

    @Override
    public boolean isInteger() {
        return reflectedClass.isInteger();
    }

    @Override
    public boolean isIterable() {
        return reflectedClass.isIterable();
    }

    @Override
    public boolean isLong() {
        return reflectedClass.isLong();
    }

    @Override
    public boolean isMap() {
        return reflectedClass.isMap();
    }

    @Override
    public boolean isOptional() {
        return reflectedClass.isOptional();
    }

    @Override
    public boolean isShort() {
        return reflectedClass.isShort();
    }

    @Override
    public boolean isString() {
        return reflectedClass.isCharacter() || reflectedClass.isString();
    }

    @Override
    public boolean isSystem() {
        return origin.getClassInfo() == null;
    }
}
