package dev.hilla.parser.models;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

final class FieldInfoReflectionModel extends FieldInfoAbstractModel<Field>
        implements ReflectionModel {
    FieldInfoReflectionModel(Field field) {
        super(field);
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

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return AnnotationUtils.convert(origin.getAnnotations());
    }

    @Override
    protected ClassInfoModel prepareOwner() {
        return ClassInfoModel.of(origin.getDeclaringClass());
    }

    @Override
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.getAnnotatedType());
    }
}
