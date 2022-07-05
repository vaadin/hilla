package dev.hilla.parser.models;

import io.github.classgraph.AnnotationEnumValue;

final class AnnotationParameterEnumValueSourceModel
        extends AnnotationParameterEnumValueModel implements SourceModel {
    private final AnnotationEnumValue origin;

    AnnotationParameterEnumValueSourceModel(AnnotationEnumValue origin) {
        this.origin = origin;
    }

    @Override
    public AnnotationEnumValue get() {
        return origin;
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
