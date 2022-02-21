package dev.hilla.parser.models;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class FieldInfoReflectionModel extends AbstractModel<Field>
        implements FieldInfoModel, ReflectionModel {
    private List<AnnotationInfoModel> annotations;
    private SignatureModel type;

    public FieldInfoReflectionModel(Field field, Model parent) {
        super(field, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getAnnotatedType(), this);
        }

        return type;
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = Arrays.stream(origin.getAnnotations())
                .map(annotation -> AnnotationInfoModel.of(annotation, this))
                .collect(Collectors.toList());
        }

        return annotations;
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
