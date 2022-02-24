package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.FieldInfo;

final class FieldInfoSourceModel extends AbstractAnnotatedSourceModel<FieldInfo>
        implements FieldInfoModel, SourceModel {
    private SignatureModel type;

    public FieldInfoSourceModel(FieldInfo field, Model parent) {
        super(field, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor(),
                    this);
        }

        return type;
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
