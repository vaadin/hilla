package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

final class MethodParameterInfoReflectionModel extends MethodParameterInfoModel
        implements ReflectionModel {
    private final Parameter origin;

    MethodParameterInfoReflectionModel(Parameter origin) {
        this.origin = origin;
    }

    @Override
    public Parameter get() {
        return origin;
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
    public boolean isFinal() {
        return Modifier.isFinal(origin.getModifiers());
    }

    @Override
    public boolean isMandated() {
        return origin.isImplicit();
    }

    @Override
    public boolean isImplicit() {
        return origin.isImplicit();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected MethodInfoModel prepareOwner() {
        return MethodInfoModel.of((Method) origin.getDeclaringExecutable());
    }

    @Override
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.getAnnotatedType());
    }
}
