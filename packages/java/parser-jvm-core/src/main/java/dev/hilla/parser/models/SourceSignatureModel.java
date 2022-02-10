package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.HierarchicalTypeSignature;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeVariableSignature;

public interface SourceSignatureModel extends SourceModel {
    static TypeModel of(HierarchicalTypeSignature signature,
            Dependable<?, ?> parent) {
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

    static TypeModel ofNullable(HierarchicalTypeSignature signature,
            Dependable<?, ?> parent) {
        return signature == null ? null : of(signature, parent);
    }

    static Stream<ClassInfo> resolve(HierarchicalTypeSignature signature) {
        if (signature == null) {
            return Stream.empty();
        }

        if (signature instanceof BaseTypeSignature) {
            return BaseSignatureModel.resolveDependencies((BaseTypeSignature) signature);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArraySignatureModel.resolveDependencies((ArrayTypeSignature) signature);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableModel.resolveDependencies((TypeVariableSignature) signature);
        } else if (signature instanceof TypeArgument) {
            return TypeArgumentModel.resolve((TypeArgument) signature);
        } else {
            return ClassRefSignatureModel
                    .resolveDependencies((ClassRefTypeSignature) signature);
        }
    }
}
