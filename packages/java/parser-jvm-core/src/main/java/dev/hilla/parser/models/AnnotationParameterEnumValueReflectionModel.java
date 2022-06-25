package dev.hilla.parser.models;

import javax.annotation.Nonnull;

final class AnnotationParameterEnumValueReflectionModel
        extends AnnotationParameterEnumValueAbstractModel<Enum<?>>
        implements ReflectionModel {
    AnnotationParameterEnumValueReflectionModel(@Nonnull Enum<?> origin) {
        super(origin);
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
