package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.ParameterizedType;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
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
        } else if (signature instanceof io.github.classgraph.TypeArgument) {
            return TypeArgumentModel
                    .of((io.github.classgraph.TypeArgument) signature, parent);
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
        } else if (signature instanceof AnnotatedTypeVariable) {
            return TypeVariableModel.of((AnnotatedTypeVariable) signature,
                    parent);
        } else if (signature instanceof AnnotatedWildcardType) {
            return TypeArgumentModel.of((AnnotatedWildcardType) signature,
                    parent);
        } else if (signature instanceof AnnotatedArrayType) {
            return ArraySignatureModel.of((AnnotatedArrayType) signature,
                    parent);
        } else if (((Class<?>) signature).isPrimitive()) {
            return BaseSignatureModel.of((Class<?>) signature, parent);
        } else {
            return ClassRefSignatureModel.of((Class<?>) signature, parent);
        }
    }

    static Stream<ClassInfo> resolveDependencies(
            HierarchicalTypeSignature signature) {
        if (signature == null) {
            return Stream.empty();
        }

        if (signature instanceof BaseTypeSignature) {
            return BaseSignatureModel
                    .resolveDependencies((BaseTypeSignature) signature);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArraySignatureModel
                    .resolveDependencies((ArrayTypeSignature) signature);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableModel
                    .resolveDependencies((TypeVariableSignature) signature);
        } else if (signature instanceof TypeArgument) {
            return TypeArgumentModel
                    .resolveDependencies((TypeArgument) signature);
        } else {
            return ClassRefSignatureModel
                    .resolveDependencies((ClassRefTypeSignature) signature);
        }
    }

    static Stream<Class<?>> resolveDependencies(AnnotatedElement signature) {
        if (signature == null) {
            return Stream.empty();
        }

        if (signature instanceof ParameterizedType) {
            return ClassRefSignatureModel.resolveDependencies(signature);
        } else if (signature instanceof AnnotatedTypeVariable) {
            return TypeVariableModel
                    .resolveDependencies((AnnotatedTypeVariable) signature);
        } else if (signature instanceof AnnotatedWildcardType) {
            return TypeArgumentModel
                    .resolveDependencies((AnnotatedWildcardType) signature);
        } else if (signature instanceof AnnotatedArrayType) {
            return ArraySignatureModel
                    .resolveDependencies((AnnotatedArrayType) signature);
        } else if (((Class<?>) signature).isPrimitive()) {
            return BaseSignatureModel.resolveDependencies(signature);
        } else {
            return ClassRefSignatureModel.resolveDependencies(signature);
        }
    }
}
