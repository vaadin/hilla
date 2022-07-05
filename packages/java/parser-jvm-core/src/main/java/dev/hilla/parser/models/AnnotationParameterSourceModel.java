package dev.hilla.parser.models;

import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationParameterValue;

final class AnnotationParameterSourceModel extends AnnotationParameterModel
        implements SourceModel {
    private final AnnotationParameterValue origin;

    AnnotationParameterSourceModel(AnnotationParameterValue origin) {
        this.origin = origin;
    }

    @Override
    public AnnotationParameterValue get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected Object prepareValue() {
        var _value = origin.getValue();

        if (_value instanceof AnnotationClassRef) {
            return ClassInfoModel
                    .of(((AnnotationClassRef) _value).getClassInfo());
        } else if (_value instanceof AnnotationEnumValue) {
            return AnnotationParameterEnumValueModel
                    .of((AnnotationEnumValue) _value);
        }

        return _value;
    }
}
