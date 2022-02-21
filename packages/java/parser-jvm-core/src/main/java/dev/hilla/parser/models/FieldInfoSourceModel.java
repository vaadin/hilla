package dev.hilla.parser.models;

import java.util.List;

import io.github.classgraph.FieldInfo;

final class FieldInfoSourceModel extends AbstractModel<FieldInfo>
        implements FieldInfoModel, SourceModel {
    private List<AnnotationInfoModel> annotations;
    private SignatureModel type;

    public FieldInfoSourceModel(FieldInfo field, Model parent) {
        super(field, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils
                    .processTypeAnnotations(origin.getAnnotationInfo(), this);
        }

        return annotations;
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
}
