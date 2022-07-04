package dev.hilla.parser.models;

import java.util.Map;
import java.util.stream.Stream;

final class AnnotationParameterReflectionModel<T>
        extends AbstractModel<Map.Entry<String, T>>
        implements AnnotationParameterModel {
    private Object value;

    AnnotationParameterReflectionModel(Map.Entry<String, T> origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationParameterModel)) {
            return false;
        }

        var other = (AnnotationParameterModel) obj;

        return getName().equals(other.getName())
                && getValue().equals(other.getValue());
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        var value = getValue();

        if (value instanceof ClassInfoModel) {
            return Stream.of((ClassInfoModel) value);
        } else if (value instanceof AnnotationParameterEnumValueModel) {
            return Stream.of(
                    ((AnnotationParameterEnumValueModel) value).getClassInfo());
        }

        return Stream.empty();
    }

    @Override
    public String getName() {
        return origin.getKey();
    }

    @Override
    public Object getValue() {
        if (value == null) {
            var _value = origin.getValue();

            if (_value instanceof Class<?>) {
                value = ClassInfoModel.of((Class<?>) _value);
            } else if (_value instanceof Enum<?>) {
                value = AnnotationParameterEnumValueModel.of((Enum<?>) _value);
            } else {
                value = _value;
            }
        }

        return value;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 7 * getValue().hashCode();
    }
}
