package dev.hilla.parser.models;

import java.util.Objects;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.FieldInfo;

final class FieldInfoSourceModel extends AbstractAnnotatedSourceModel<FieldInfo>
        implements FieldInfoModel, SourceModel {
    private ClassInfoModel owner;
    private SignatureModel type;

    public FieldInfoSourceModel(FieldInfo field) {
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
            owner = ClassInfoModel.of(origin.getClassInfo());
        }

        return owner;
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor());
        }

        return type;
    }

    @Override
    public int hashCode() {
        return origin.hashCode();
    }

    @Override
    public boolean isEnum() {
        return origin.isEnum();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
    }

    @Override
    public boolean isPrivate() {
        return origin.isPrivate();
    }

    @Override
    public boolean isProtected() {
        return origin.isProtected();
    }

    @Override
    public boolean isPublic() {
        return origin.isPublic();
    }

    @Override
    public boolean isStatic() {
        return origin.isStatic();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isTransient() {
        return origin.isTransient();
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getAnnotationInfo().stream();
    }
}
