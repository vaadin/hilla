package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.BaseTypeSignature;

final class BaseSignatureSourceModel
        extends AbstractAnnotatedSourceModel<BaseTypeSignature>
        implements BaseSignatureModel, SourceSignatureModel {
    public BaseSignatureSourceModel(BaseTypeSignature origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BaseSignatureModel)) {
            return false;
        }

        var other = (BaseSignatureModel) obj;

        return origin.getType().equals(other.getType())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public Class<?> getType() {
        return origin.getType();
    }

    @Override
    public int hashCode() {
        return 7 + origin.getType().hashCode();
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

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();
        return annotations != null ? annotations.stream() : Stream.empty();
    }
}
