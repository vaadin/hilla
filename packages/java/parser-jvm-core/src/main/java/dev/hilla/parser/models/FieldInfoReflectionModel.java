package dev.hilla.parser.models;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

final class FieldInfoReflectionModel
        extends AbstractAnnotatedReflectionModel<Field>
        implements FieldInfoModel, ReflectionModel {
    private List<AnnotationInfoModel> annotations;
    private ClassInfoModel owner;
    private SignatureModel type;

    public FieldInfoReflectionModel(Field field) {
        super(field);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof FieldInfoModel)) {
            return false;
        }

        var model = (FieldInfoModel) other;

        return Objects.equals(getOwner(), model.getOwner())
                && Objects.equals(origin.getName(), model.getName());
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = ClassInfoModel.of(origin.getDeclaringClass());
        }

        return owner;
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getAnnotatedType());
        }

        return type;
    }

    @Override
    public int hashCode() {
        return origin.getName().hashCode() + getOwner().hashCode() * 11;
    }

    @Override
    public boolean isEnum() {
        return origin.isEnumConstant();
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(origin.getModifiers());
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(origin.getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(origin.getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(origin.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(origin.getModifiers());
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isTransient() {
        return Modifier.isTransient(origin.getModifiers());
    }
}
