package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

final class AnnotationInfoReflectionModel extends AnnotationInfoModel
        implements ReflectionModel {
    private final Annotation origin;

    AnnotationInfoReflectionModel(Annotation origin) {
        this.origin = origin;
    }

    @Override
    public Annotation get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.annotationType().getName();
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return ClassInfoModel.of(origin.annotationType());
    }

    @Override
    protected Set<AnnotationParameterModel> prepareParameters() {
        try {
            var methods = origin.annotationType().getDeclaredMethods();

            var parameters = new HashSet<AnnotationParameterModel>(
                    methods.length);

            for (var method : methods) {
                parameters.add(AnnotationParameterModel.of(method.getName(),
                        method.invoke(origin)));
            }

            return parameters;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ModelException(e);
        }
    }
}
