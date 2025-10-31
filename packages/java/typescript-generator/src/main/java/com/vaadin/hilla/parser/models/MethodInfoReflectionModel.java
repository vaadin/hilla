package com.vaadin.hilla.parser.models;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract class MethodInfoReflectionModel extends MethodInfoModel
        implements ReflectionModel {
    @Override
    public abstract Executable get();

    @Override
    public String getClassName() {
        return get().getDeclaringClass().getName();
    }

    @Override
    public int getModifiers() {
        return get().getModifiers();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    @Override
    public boolean isNative() {
        return Modifier.isNative(getModifiers());
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    @Override
    public boolean isStrict() {
        return Modifier.isStrict(getModifiers());
    }

    @Override
    public boolean isSynchronized() {
        return Modifier.isSynchronized(getModifiers());
    }

    @Override
    public boolean isSynthetic() {
        return get().isSynthetic();
    }

    @Override
    public boolean isVarArgs() {
        return get().isVarArgs();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(get().getAnnotations());
    }

    @Override
    protected ClassInfoModel prepareOwner() {
        return ClassInfoModel.of(get().getDeclaringClass());
    }

    @Override
    protected List<MethodParameterInfoModel> prepareParameters() {
        return Arrays.stream(get().getParameters())
                .map(MethodParameterInfoModel::of).collect(Collectors.toList());
    }

    static final class Constructor extends MethodInfoReflectionModel {
        private static final String constructorName = "<init>";

        private final java.lang.reflect.Constructor<?> origin;

        Constructor(java.lang.reflect.Constructor<?> origin) {
            this.origin = origin;
        }

        @Override
        public java.lang.reflect.Constructor<?> get() {
            return origin;
        }

        @Override
        public String getName() {
            return constructorName;
        }

        @Override
        public boolean isBridge() {
            return false;
        }

        @Override
        public boolean isConstructor() {
            return true;
        }

        @Override
        protected SignatureModel prepareResultType() {
            return BaseSignatureModel.of(Void.TYPE);
        }

        @Override
        protected List<TypeParameterModel> prepareTypeParameters() {
            return List.of();
        }
    }

    static final class Regular extends MethodInfoReflectionModel {
        private final Method origin;

        Regular(Method origin) {
            this.origin = origin;
        }

        @Override
        public Method get() {
            return origin;
        }

        @Override
        public String getName() {
            return origin.getName();
        }

        @Override
        public boolean isBridge() {
            return origin.isBridge();
        }

        @Override
        public boolean isConstructor() {
            return false;
        }

        @Override
        protected SignatureModel prepareResultType() {
            return SignatureModel.of(origin.getAnnotatedReturnType());
        }

        @Override
        protected List<TypeParameterModel> prepareTypeParameters() {
            return Arrays.stream(origin.getTypeParameters())
                    .map(TypeParameterModel::of).collect(Collectors.toList());
        }
    }
}
