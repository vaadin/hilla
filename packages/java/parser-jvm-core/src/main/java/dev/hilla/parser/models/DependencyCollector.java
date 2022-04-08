package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.HierarchicalTypeSignature;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeVariableSignature;

public interface DependencyCollector<S, D> {
    static Stream<ClassInfoModel> collect(AnnotatedElement signature) {
        var collector = new Reflection();
        collector.collect(signature);

        return collector.getDependencies().stream()
                .filter(ClassInfoModel::isNonJDKClass).map(ClassInfoModel::of);
    }

    static Stream<ClassInfoModel> collect(HierarchicalTypeSignature signature) {
        var collector = new Source();
        collector.collect(signature);

        return collector.getDependencies().stream()
                .filter(ClassInfoModel::isNonJDKClass).map(ClassInfoModel::of);
    }

    void collect(S signature);

    Set<D> getDependencies();

    final class Reflection
            implements DependencyCollector<AnnotatedElement, Class<?>> {
        private final Set<Class<?>> dependencies = new LinkedHashSet<>();

        private Reflection() {
        }

        @Override
        public void collect(AnnotatedElement signature) {
            if (signature == null) {
                return;
            }

            if (signature instanceof AnnotatedParameterizedType) {
                collectAnnotatedParameterizedType(
                        (AnnotatedParameterizedType) signature);
            } else if (signature instanceof AnnotatedArrayType) {
                collectAnnotatedArrayType((AnnotatedArrayType) signature);
            } else if (signature instanceof AnnotatedTypeVariable) {
                collectAnnotatedTypeVariable((AnnotatedTypeVariable) signature);
            } else if (signature instanceof AnnotatedWildcardType) {
                collectAnnotatedWildcardType((AnnotatedWildcardType) signature);
            } else {
                var type = signature instanceof AnnotatedType
                        ? (Class<?>) ((AnnotatedType) signature).getType()
                        : (Class<?>) signature;

                if (type.isPrimitive()) {
                    collectPrimitive(type);
                } else {
                    collectClass(type);
                }
            }
        }

        @Override
        public Set<Class<?>> getDependencies() {
            return dependencies;
        }

        private void collectAnnotatedArrayType(AnnotatedArrayType signature) {
            collect(signature.getAnnotatedGenericComponentType());
        }

        private void collectAnnotatedParameterizedType(
                AnnotatedParameterizedType signature) {
            var isNewDependency = collectClass(
                    (Class<?>) ((ParameterizedType) signature.getType())
                            .getRawType());

            if (isNewDependency) {
                for (var argument : signature
                        .getAnnotatedActualTypeArguments()) {
                    collect(argument);
                }
            }
        }

        private void collectAnnotatedTypeVariable(
                AnnotatedTypeVariable signature) {
            // We can resolve only the type variable class bound here (bound
            // class is `dev.hilla.X` in `T extends dev.hilla.X`)
            var bound = signature.getAnnotatedBounds()[0];

            if (bound != null) {
                collect(bound);
            }
        }

        private void collectAnnotatedWildcardType(
                AnnotatedWildcardType signature) {
            for (var bound : signature.getAnnotatedUpperBounds()) {
                collect(bound);
            }

            for (var bound : signature.getAnnotatedLowerBounds()) {
                collect(bound);
            }
        }

        private boolean collectClass(Class<?> signature) {
            return dependencies.add(signature);
        }

        private void collectPrimitive(Class<?> signature) {
            // BaseType is about primitive types (int, double, etc.).
            // We don't need to resolve them, so skipping.
        }
    }

    final class Source implements
            DependencyCollector<HierarchicalTypeSignature, ClassInfo> {
        private final Set<ClassInfo> dependencies = new LinkedHashSet<>();

        private Source() {
        }

        @Override
        public void collect(HierarchicalTypeSignature signature) {
            if (signature == null) {
                return;
            }

            if (signature instanceof BaseTypeSignature) {
                collectBaseTypeSignature((BaseTypeSignature) signature);
            } else if (signature instanceof ArrayTypeSignature) {
                collectArrayTypeSignature((ArrayTypeSignature) signature);
            } else if (signature instanceof TypeVariableSignature) {
                collectTypeVariableSignature((TypeVariableSignature) signature);
            } else if (signature instanceof TypeArgument) {
                collectTypeArgument((TypeArgument) signature);
            } else {
                collectClassRefTypeSignature((ClassRefTypeSignature) signature);
            }
        }

        @Override
        public Set<ClassInfo> getDependencies() {
            return dependencies;
        }

        private void collectArrayTypeSignature(ArrayTypeSignature signature) {
            collect(signature.getElementTypeSignature());
        }

        private void collectBaseTypeSignature(BaseTypeSignature signature) {
            // BaseType is about primitive types (int, double, etc.).
            // We don't need to resolve them, so skipping.
        }

        private void collectClassRefTypeSignature(
                ClassRefTypeSignature signature) {
            var classInfo = signature.getClassInfo();

            if (classInfo != null) {
                dependencies.add(classInfo);
            }

            for (var argument : signature.getTypeArguments()) {
                collect(argument);
            }
        }

        private void collectTypeArgument(TypeArgument signature) {
            collect(signature.getTypeSignature());
        }

        private void collectTypeVariableSignature(
                TypeVariableSignature signature) {
            // We can resolve only the type variable class bound here (bound
            // class is `dev.hilla.X` in `T extends dev.hilla.X` / `T super
            // dev.hilla.X`)
            var bound = signature.resolve().getClassBound();

            if (bound != null) {
                collect(bound);
            }
        }
    }
}
