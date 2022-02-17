package dev.hilla.parser.models;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;

final class ClassInfoReflectionModel extends AbstractModel<Class<?>>
        implements ClassInfoModel, ReflectionModel {
    private final ClassInfoModelInheritanceChain chain;
    private final ClassInfoModel superClass;
    private Collection<AnnotationInfoModel> annotations;
    private Collection<FieldInfoModel> fields;
    private Collection<ClassInfoModel> innerClasses;
    private Collection<MethodInfoModel> methods;
    private Collection<ClassInfoModel> superClasses;

    public ClassInfoReflectionModel(Class<?> origin, Model parent) {
        super(origin, parent);

        var superClass = origin.getSuperclass();

        this.superClass = superClass != null
                && !Objects.equals(superClass, Object.class)
                        ? ClassInfoModel.of(superClass)
                        : null;

        this.chain = new ClassInfoModelInheritanceChain(this);
    }

    @Override
    public Collection<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = getMembers(origin.getAnnotations(),
                    AnnotationInfoModel::of);
        }

        return annotations;
    }

    @Override
    public Collection<FieldInfoModel> getFields() {
        if (fields == null) {
            fields = getMembers(origin.getDeclaredFields(), FieldInfoModel::of);
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
            innerClasses = getMembers(origin.getDeclaredClasses(),
                    ClassInfoModel::of);
        }

        return innerClasses;
    }

    @Override
    public Collection<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = getMembers(origin.getDeclaredMethods(),
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
            superClasses = new LinkedHashSet<>();

            var cls = origin.getSuperclass();

            while (cls != null && cls != Object.class) {
                superClasses.add(ClassInfoModel.of(cls));
                cls = cls.getSuperclass();
            }
        }

        return superClasses;
    }
}
