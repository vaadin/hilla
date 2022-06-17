package dev.hilla.parser.models;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FieldInfoModel)) {
            return false;
        }

        var other = (FieldInfoModel) obj;

        return origin.getDeclaringClass().getName().equals(other.getClassName())
                && origin.getName().equals(other.getName());
    }

    @Override
    public String getClassName() {
        return origin.getDeclaringClass().getName();
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
        return origin.getName().hashCode()
                + 11 * origin.getDeclaringClass().getName().hashCode();
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
