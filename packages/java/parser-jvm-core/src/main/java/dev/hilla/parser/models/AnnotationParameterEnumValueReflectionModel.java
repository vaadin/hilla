package dev.hilla.parser.models;

final class AnnotationParameterEnumValueReflectionModel
        extends AnnotationParameterEnumValueModel implements ReflectionModel {
    private final Enum<?> origin;

    AnnotationParameterEnumValueReflectionModel(Enum<?> origin) {
        this.origin = origin;
    }

    @Override
    public Enum<?> get() {
        return origin;
    }

    @Override
    public String getValueName() {
        return origin.toString();
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return ClassInfoModel.of(origin.getClass());
    }
}
