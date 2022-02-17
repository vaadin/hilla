package dev.hilla.parser.models;

import static dev.hilla.parser.models.ModelUtils.isJDKClass;

import java.util.Collection;
import java.util.Optional;

import io.github.classgraph.ClassInfo;

final class ClassInfoSourceModel extends AbstractModel<ClassInfo>
        implements ClassInfoModel, SourceModel {
    private final ClassInfoModelInheritanceChain chain;
    private final ClassInfoModel superClass;
    private Collection<AnnotationInfoModel> annotations;
    private Collection<FieldInfoModel> fields;
    private Collection<ClassInfoModel> innerClasses;
    private Collection<MethodInfoModel> methods;
    private Collection<ClassInfoModel> superClasses;

    public ClassInfoSourceModel(ClassInfo origin, Model parent) {
        super(origin, parent);

        var originSuperClass = origin.getSuperclass();
        superClass = originSuperClass != null
                ? ClassInfoModel.of(originSuperClass)
                : null;

        chain = new ClassInfoModelInheritanceChain(this);
    }

    @Override
    public Collection<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = getMembers(origin.getAnnotationInfo(),
                    AnnotationInfoModel::of);
        }

        return annotations;
    }

    @Override
    public Collection<FieldInfoModel> getFields() {
        if (fields == null) {
            fields = getMembers(origin.getDeclaredFieldInfo(),
                    FieldInfoModel::of);
        }

        return fields;
    }

    @Override
    public ClassInfoModelInheritanceChain getInheritanceChain() {
        return chain;
    }

    @Override
    public Collection<ClassInfoModel> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = getMembers(origin.getInnerClasses(),
                    ClassInfoModel::of);
        }

        return innerClasses;
    }

    @Override
    public Collection<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = getMembers(origin.getDeclaredMethodInfo(),
                    MethodInfoModel::of);
        }

        return methods;
    }

    @Override
    public Optional<ClassInfoModel> getSuperClass() {
        return Optional.ofNullable(superClass);
    }

    @Override
    public Collection<ClassInfoModel> getSuperClasses() {
        if (superClasses == null) {
            superClasses = getMembers(origin.getSuperclasses(),
                    (member) -> !isJDKClass(member), ClassInfoModel::of);
        }

        return superClasses;
    }
}
