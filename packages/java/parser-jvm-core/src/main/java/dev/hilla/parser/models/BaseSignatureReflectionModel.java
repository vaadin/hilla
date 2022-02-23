package dev.hilla.parser.models;

import java.util.List;

final class BaseSignatureReflectionModel extends AbstractModel<Class<?>>
        implements BaseSignatureModel, ReflectionSignatureModel {
    private List<AnnotationInfoModel> annotations;

    public BaseSignatureReflectionModel(Class<?> origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = new AnnotationProcessor.Reflection(this).add(origin)
                .process();
        }

        return annotations;
    }

    @Override
    public boolean isBoolean() {
        return origin == Boolean.TYPE;
    }

    @Override
    public boolean isByte() {
        return origin == Byte.TYPE;
    }

    @Override
    public boolean isCharacter() {
        return origin == Character.TYPE;
    }

    @Override
    public boolean isDouble() {
        return origin == Double.TYPE;
    }

    @Override
    public boolean isFloat() {
        return origin == Float.TYPE;
    }

    @Override
    public boolean isInteger() {
        return origin == Integer.TYPE;
    }

    @Override
    public boolean isJDKClass() {
        return true;
    }

    @Override
    public boolean isLong() {
        return origin == Long.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        return origin != Void.TYPE;
    }

    @Override
    public boolean isShort() {
        return origin == Short.TYPE;
    }

    @Override
    public boolean isVoid() {
        return origin == Void.TYPE;
    }
}
