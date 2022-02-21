package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.BaseTypeSignature;

final class BaseSignatureSourceModel extends AbstractModel<BaseTypeSignature>
        implements BaseSignatureModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;

    public BaseSignatureSourceModel(BaseTypeSignature origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils.processTypeAnnotations(origin.getTypeAnnotationInfo(), this);
        }

        return annotations;
    }

    @Override
    public boolean isBoolean() {
        return origin.getType() == Boolean.TYPE;
    }

    @Override
    public boolean isByte() {
        return origin.getType() == Byte.TYPE;
    }

    @Override
    public boolean isCharacter() {
        return origin.getType() == Character.TYPE;
    }

    @Override
    public boolean isDouble() {
        return origin.getType() == Double.TYPE;
    }

    @Override
    public boolean isFloat() {
        return origin.getType() == Float.TYPE;
    }

    @Override
    public boolean isInteger() {
        return origin.getType() == Integer.TYPE;
    }

    @Override
    public boolean isJDKClass() {
        return true;
    }

    @Override
    public boolean isLong() {
        return origin.getType() == Long.TYPE;
    }

    @Override
    public boolean isPrimitive() {
        var type = origin.getType();

        return type != null && type != Void.TYPE;
    }

    @Override
    public boolean isShort() {
        return origin.getType() == Short.TYPE;
    }

    @Override
    public boolean isVoid() {
        return origin.getType() == Void.TYPE;
    }
}
