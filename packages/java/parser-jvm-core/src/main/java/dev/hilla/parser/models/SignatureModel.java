package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.HierarchicalTypeSignature;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeVariableSignature;

public interface SignatureModel
        extends Model, SpecializedModel, AnnotatedModel {
    static SignatureModel of(@Nonnull HierarchicalTypeSignature signature,
            Model parent) {
        if (signature instanceof BaseTypeSignature) {
            return BaseSignatureModel.of((BaseTypeSignature) signature, parent);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArraySignatureModel.of((ArrayTypeSignature) signature,
                    parent);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableModel.of((TypeVariableSignature) signature,
                    parent);
        } else if (signature instanceof TypeArgument) {
            return TypeArgumentModel.of((TypeArgument) signature, parent);
        } else {
            return ClassRefSignatureModel.of((ClassRefTypeSignature) signature,
                    parent);
        }
    }

    static SignatureModel of(@Nonnull AnnotatedElement signature,
            Model parent) {
        if (signature instanceof AnnotatedParameterizedType) {
            return ClassRefSignatureModel
                    .of((AnnotatedParameterizedType) signature, parent);
        } else if (signature instanceof AnnotatedArrayType) {
            return ArraySignatureModel.of((AnnotatedArrayType) signature,
                    parent);
        } else if (signature instanceof AnnotatedTypeVariable) {
            return TypeVariableModel.of((AnnotatedTypeVariable) signature,
                    parent);
        } else if (signature instanceof AnnotatedWildcardType) {
            return TypeArgumentModel.of((AnnotatedWildcardType) signature,
                    parent);
        } else {
            var type = signature instanceof AnnotatedType
                    ? (Class<?>) ((AnnotatedType) signature).getType()
                    : (Class<?>) signature;

            if (type.isPrimitive()) {
                return BaseSignatureModel.of(type, parent);
            } else {
                return ClassRefSignatureModel.of(type, parent);
            }
        }
    }
}
