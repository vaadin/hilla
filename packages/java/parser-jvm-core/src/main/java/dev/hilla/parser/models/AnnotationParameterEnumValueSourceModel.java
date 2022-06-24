package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationEnumValue;

final class AnnotationParameterEnumValueSourceModel
        extends AbstractModel<AnnotationEnumValue>
        implements AnnotationParameterEnumValueModel {
    private ClassInfoModel classInfo;

    AnnotationParameterEnumValueSourceModel(AnnotationEnumValue origin) {
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
        try {
            if (classInfo == null) {
                classInfo = ClassInfoModel
                        .of(Class.forName(origin.getClassName()));
            }

            return classInfo;
        } catch (ClassNotFoundException e) {
            throw new ModelException(e);
        }
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        return Stream.of(getClassInfo());
    }

    @Override
    public String getValueName() {
        return origin.getValueName();
    }

    @Override
    public int hashCode() {
        return getClassInfo().hashCode() + 13 * getValueName().hashCode();
    }
}
