package dev.hilla.parser.models;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

abstract class BaseSignatureAbstractModel<T> extends AbstractModel<T>
        implements BaseSignatureModel {
    private List<AnnotationInfoModel> annotations;

    BaseSignatureAbstractModel(@Nonnull T origin) {
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

        return getType().equals(other.getType())
                && Objects.equals(getAnnotations(), other.getAnnotations());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = prepareAnnotations();
        }

        return annotations;
    }

    @Override
    public int hashCode() {
        return 7 + getType().hashCode();
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();
}
