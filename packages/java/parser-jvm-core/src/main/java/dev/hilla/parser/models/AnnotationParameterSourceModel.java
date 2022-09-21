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
            var _ref = (AnnotationClassRef) _value;
            if (_ref.getClassInfo() == null) {
                // ClassGraph is missing the class, try loading from reflection
                return ClassInfoModel.of(_ref.loadClass());
            } else {
                return ClassInfoModel.of(_ref.getClassInfo());
            }
        } else if (_value instanceof AnnotationEnumValue) {
            return AnnotationParameterEnumValueModel
                    .of((AnnotationEnumValue) _value);
        }

        return _value;
    }
}
