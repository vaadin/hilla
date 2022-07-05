package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

final class AnnotationInfoReflectionModel extends AbstractModel<Annotation>
        implements AnnotationInfoModel, ReflectionModel {
    private Set<AnnotationParameterModel> parameters;
    private ClassInfoModel resolved;

    public AnnotationInfoReflectionModel(Annotation annotation) {
        super(annotation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationInfoModel)) {
            return false;
        }

        var other = (AnnotationInfoModel) obj;

        return getName().equals(other.getName())
                && getParameters().equals(other.getParameters());
    }

    @Override
    public ClassInfoModel getClassInfo() {
        if (resolved == null) {
            resolved = ClassInfoModel.of(origin.annotationType());
        }

        return resolved;
    }

    @Override
    public String getName() {
        return origin.annotationType().getName();
    }

    @Override
    public Set<AnnotationParameterModel> getParameters() {
        if (parameters == null) {
            parameters = Arrays
                    .stream(origin.annotationType().getDeclaredMethods())
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

        return parameters;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 11 * getParameters().hashCode();
    }
}
