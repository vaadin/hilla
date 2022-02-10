package dev.hilla.parser.models;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.stream.Stream;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.TypeVariableSignature;

public interface ReflectionSignatureModel extends ReflectionModel {
    static TypeModel of(Type signature, Dependable<?, ?> parent) {
        if (signature instanceof Class<?>) {
            if (((Class<?>) signature).isPrimitive()) {
                return BaseSignatureModel.of((Class<?>) signature, parent);
            } else if (((Class<?>) signature).isArray()) {
                return ArraySignatureModel.of((Class<?>) signature, parent);
            } else {
                return ClassRefSignatureModel.of((Class<?>) signature, parent);
            }
        } else if (signature instanceof TypeVariable<?>) {
            return TypeVariableModel.of((TypeVariable<?>) signature, parent);
        } else if (signature instanceof WildcardType) {
            return TypeArgumentModel.of((WildcardType) signature, parent);
        }
    }

    static Stream<Class<?>> resolve(Type signature) {
        if (signature == null) {
            return Stream.empty();
        }

        if (signature instanceof Class<?>) {
            if (((Class<?>) signature).isPrimitive()) {
                return BaseSignatureModel.resolveDependencies((Class<?>) signature);
            } else if (((Class<?>) signature).isArray()) {
                ArraySignatureModel.resolveDependencies((Class<?>) signature);
            } else {
                ClassRefSignatureModel.resolveDependencies((Class<?>) signature);
            }
        }

        if (signature instanceof BaseTypeSignature) {
            return BaseSignatureModel.resolve(signature);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArraySignatureSourceModel.resolve(signature);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableSourceModel
                    .resolveDependencies((TypeVariableSignature) signature);
        } else if (signature instanceof io.github.classgraph.TypeArgument) {
            return TypeArgumentSourceModel
                    .resolve((io.github.classgraph.TypeArgument) signature);
        } else {
            return ClassRefSignatureSourceModel
                    .resolve((ClassRefTypeSignature) signature);
        }
    }
}
