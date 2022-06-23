package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

final class AnnotationInfoReflectionModel extends AbstractModel<Annotation>
        implements AnnotationInfoModel, ReflectionModel {
    private Set<AnnotationParameter> parameters;
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
    public Set<AnnotationParameter> getParameters() {
        if (parameters == null) {
            var methods = origin.annotationType().getDeclaredMethods();

            parameters = new HashSet<>(methods.length);

            for (var method : methods) {
                try {
                    parameters.add(new AnnotationParameter(method.getName(),
                            method.invoke(origin)));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new ModelException(e);
                }
            }
        }

        return parameters;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 11 * getParameters().hashCode();
    }
}
