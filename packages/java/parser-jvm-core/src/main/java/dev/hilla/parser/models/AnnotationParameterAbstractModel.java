package dev.hilla.parser.models;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

abstract class AnnotationParameterAbstractModel<T> extends AbstractModel<T>
        implements AnnotationParameterModel {
    private Object value;

    AnnotationParameterAbstractModel(@Nonnull T origin) {
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
    public Object getValue() {
        if (value == null) {
            value = prepareValue();
        }

        return value;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 7 * getValue().hashCode();
    }

    protected abstract Object prepareValue();
}
