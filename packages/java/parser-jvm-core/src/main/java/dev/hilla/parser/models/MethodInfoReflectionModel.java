package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class MethodInfoReflectionModel extends MethodInfoModel
        implements ReflectionModel {
    private final Method origin;

    MethodInfoReflectionModel(Method origin) {
        this.origin = origin;
    }

    @Override
    public boolean equalsIgnoreParameters(MethodInfoModel other) {
        return origin.getName().equals(other.getName())
                && origin.getModifiers() == other.getModifiers()
                && getResultType().equals(other.getResultType())
                && origin.getDeclaringClass().getName()
                        .equals(other.getClassName());
    }

    @Override
    public Method get() {
        return origin;
    }

    @Override
    public String getClassName() {
        return origin.getDeclaringClass().getName();
    }

    @Override
    public int getModifiers() {
        return origin.getModifiers();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public int hashCodeIgnoreParameters() {
        return origin.getName().hashCode() + 11 * getResultType().hashCode()
                + 17 * origin.getModifiers()
                + 23 * origin.getDeclaringClass().getName().hashCode();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(origin.getModifiers());
    }

    @Override
    public boolean isBridge() {
        return origin.isBridge();
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(origin.getModifiers());
    }

    @Override
    public boolean isNative() {
        return Modifier.isNative(origin.getModifiers());
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(origin.getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(origin.getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(origin.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(origin.getModifiers());
    }

    @Override
    public boolean isStrict() {
        return Modifier.isStrict(origin.getModifiers());
    }

    @Override
    public boolean isSynchronized() {
        return Modifier.isSynchronized(origin.getModifiers());
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isVarArgs() {
        return origin.isVarArgs();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected ClassInfoModel prepareOwner() {
        return ClassInfoModel.of(origin.getDeclaringClass());
    }

    @Override
    protected List<MethodParameterInfoModel> prepareParameters() {
        return Arrays.stream(origin.getParameters())
                .map(MethodParameterInfoModel::of).collect(Collectors.toList());
    }

    @Override
    protected SignatureModel prepareResultType() {
        return SignatureModel.of(origin.getAnnotatedReturnType());
    }
}
