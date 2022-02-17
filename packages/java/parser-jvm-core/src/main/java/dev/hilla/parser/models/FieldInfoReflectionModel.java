package dev.hilla.parser.models;

import java.lang.reflect.Field;

final class FieldInfoReflectionModel extends AbstractModel<Field>
        implements FieldInfoModel, ReflectionModel {
    private SignatureModel type;

    public FieldInfoReflectionModel(Field field, Model parent) {
        super(field, parent);
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getAnnotatedType(), this);
        }

        return type;
    }
}
