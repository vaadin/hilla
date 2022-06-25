package dev.hilla.parser.models;

import io.github.classgraph.AnnotationEnumValue;

final class AnnotationParameterEnumValueSourceModel
        extends AnnotationParameterEnumValueAbstractModel<AnnotationEnumValue>
        implements SourceModel {
    AnnotationParameterEnumValueSourceModel(AnnotationEnumValue origin) {
        super(origin);
    }

    @Override
    public String getValueName() {
        return origin.getValueName();
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        try {
            return ClassInfoModel.of(Class.forName(origin.getClassName()));
        } catch (ClassNotFoundException e) {
            throw new ModelException(e);
        }
    }
}
