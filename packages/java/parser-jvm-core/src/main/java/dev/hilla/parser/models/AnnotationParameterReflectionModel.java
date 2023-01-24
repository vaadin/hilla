package dev.hilla.parser.models;

final class AnnotationParameterReflectionModel<T>
        extends AnnotationParameterModel implements ReflectionModel {
    private final ReflectionOrigin<T> origin;

    AnnotationParameterReflectionModel(ReflectionOrigin<T> origin) {
        this.origin = origin;
    }

    @Override
    public ReflectionOrigin<T> get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public boolean isDefault() {
        return origin.isDefault();
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
