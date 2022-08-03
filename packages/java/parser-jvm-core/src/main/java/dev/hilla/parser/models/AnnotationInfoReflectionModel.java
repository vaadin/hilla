package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    protected Optional<ClassInfoModel> prepareClassInfo() {
        return Optional.of(ClassInfoModel.of(origin.annotationType()));
    }

    @Override
    protected Set<AnnotationParameterModel> prepareParameters() {
        return Arrays.stream(origin.annotationType().getDeclaredMethods())
                .map(method -> {
                    // Here we go through all the methods/parameters of the
                    // annotation instance and collect their values. Since
                    // annotations methods cannot be private or virtual, we
                    // could simply invoke the method to get a value.
                    try {
                        return AnnotationParameterModel.of(method.getName(),
                                method.invoke(origin));
                    } catch (InvocationTargetException
                            | IllegalAccessException e) {
                        throw new ModelException(e);
                    }
                }).collect(Collectors.toSet());
    }
}
