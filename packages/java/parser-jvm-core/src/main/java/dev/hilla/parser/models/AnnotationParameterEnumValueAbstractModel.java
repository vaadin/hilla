package dev.hilla.parser.models;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

abstract class AnnotationParameterEnumValueAbstractModel<T>
        extends AbstractModel<T> implements AnnotationParameterEnumValueModel {
    private ClassInfoModel classInfo;

    AnnotationParameterEnumValueAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationParameterEnumValueModel)) {
            return false;
        }

        var other = (AnnotationParameterEnumValueModel) obj;

        return getClassInfo().equals(other.getClassInfo())
                && getValueName().equals(other.getValueName());
    }

    @Override
    public ClassInfoModel getClassInfo() {
        if (classInfo == null) {
            classInfo = prepareClassInfo();
        }

        return classInfo;
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        return Stream.of(getClassInfo());
    }

    @Override
    public int hashCode() {
        return getClassInfo().hashCode() + 13 * getValueName().hashCode();
    }

    protected abstract ClassInfoModel prepareClassInfo();
}
