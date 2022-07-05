package dev.hilla.parser.models;

import java.util.Map;

final class AnnotationParameterReflectionModel<T>
        extends AnnotationParameterModel implements ReflectionModel {
    private final Map.Entry<String, T> origin;

    AnnotationParameterReflectionModel(Map.Entry<String, T> origin) {
        this.origin = origin;
    }

    @Override
    public Map.Entry<String, T> get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getKey();
    }

    @Override
    protected Object prepareValue() {
        var value = origin.getValue();

        if (value instanceof Class<?>) {
            return ClassInfoModel.of((Class<?>) value);
        } else if (value instanceof Enum<?>) {
            return AnnotationParameterEnumValueModel.of((Enum<?>) value);
        }

        return value;
    }
}
