package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationParameterValue;

final class AnnotationParameterSourceModel
        extends AbstractModel<AnnotationParameterValue>
        implements AnnotationParameterModel {
    private Object value;

    AnnotationParameterSourceModel(AnnotationParameterValue origin) {
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
        return origin.getName();
    }

    @Override
    public Object getValue() {
        if (value == null) {
            var _value = origin.getValue();

            if (_value instanceof AnnotationClassRef) {
                value = ClassInfoModel
                        .of(((AnnotationClassRef) _value).getClassInfo());
            } else if (_value instanceof AnnotationEnumValue) {
                value = AnnotationParameterEnumValueModel
                        .of((AnnotationEnumValue) _value);
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
