package com.vaadin.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.TypeVariable;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.HierarchicalTypeSignature;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeParameter;
import io.github.classgraph.TypeVariableSignature;

public interface SignatureModel
        extends Model, SpecializedModel, AnnotatedModel {
    @Deprecated
    static SignatureModel of(@Nonnull HierarchicalTypeSignature signature) {
        if (signature instanceof BaseTypeSignature) {
            return BaseSignatureModel.of((BaseTypeSignature) signature);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArraySignatureModel.of((ArrayTypeSignature) signature);
        } else if (signature instanceof TypeParameter) {
            return TypeParameterModel.of((TypeParameter) signature);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableModel.of((TypeVariableSignature) signature);
        } else if (signature instanceof TypeArgument) {
            return TypeArgumentModel.of((TypeArgument) signature);
        } else {
            return ClassRefSignatureModel.of((ClassRefTypeSignature) signature);
        }
    }

    static SignatureModel of(@Nonnull AnnotatedElement signature) {
        if (signature instanceof AnnotatedParameterizedType) {
            return ClassRefSignatureModel
                    .of((AnnotatedParameterizedType) signature);
        } else if (signature instanceof AnnotatedArrayType) {
            return ArraySignatureModel.of((AnnotatedArrayType) signature);
        } else if (signature instanceof TypeVariable<?>) {
            return TypeParameterModel.of((TypeVariable<?>) signature);
        } else if (signature instanceof AnnotatedTypeVariable) {
            return TypeVariableModel.of((AnnotatedTypeVariable) signature);
        } else if (signature instanceof AnnotatedWildcardType) {
            return TypeArgumentModel.of((AnnotatedWildcardType) signature);
        } else if (signature instanceof AnnotatedType) {
            var type = (Class<?>) ((AnnotatedType) signature).getType();

            if (type.isPrimitive()) {
                return BaseSignatureModel.of((AnnotatedType) signature);
            } else {
                return ClassRefSignatureModel.of((AnnotatedType) signature);
            }
        } else {
            var type = (Class<?>) signature;

            if (type.isPrimitive()) {
                return BaseSignatureModel.of(type);
            } else {
                return ClassRefSignatureModel.of(type);
            }
        }
    }
}
