package dev.hilla.parser.models;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;

final class ClassInfoSourceModel extends AbstractAnnotatedSourceModel<ClassInfo>
        implements ClassInfoModel, SourceModel {
    private final ClassInfoModelInheritanceChain chain;
    private final ClassInfoModel superClass;
    private List<FieldInfoModel> fields;
    private List<ClassInfoModel> innerClasses;
    private List<MethodInfoModel> methods;
    private List<ClassInfoModel> superClasses;

    public ClassInfoSourceModel(ClassInfo origin, Model parent) {
        super(origin, parent);

        var originSuperClass = origin.getSuperclass();
        superClass = originSuperClass != null
                ? ClassInfoModel.of(originSuperClass)
                : null;

        chain = new ClassInfoModelInheritanceChain(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ClassInfoModel)) {
            return false;
        }

        if (other instanceof ClassInfoSourceModel) {
            return Objects.equals(origin,
                    ((ClassInfoSourceModel) other).origin);
        }

        return Objects.equals(getName(), ((ClassInfoModel) other).getName());
    }

    @Override
    public List<FieldInfoModel> getFields() {
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
    public List<ClassInfoModel> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = getMembers(origin.getInnerClasses(),
                    ClassInfoModel::of);
        }

        return innerClasses;
    }

    @Override
    public List<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = getMembers(origin.getDeclaredMethodInfo(),
                    MethodInfoModel::of);
        }

        return methods;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public String getSimpleName() {
        return origin.getSimpleName();
    }

    @Override
    public Optional<ClassInfoModel> getSuperClass() {
        return Optional.ofNullable(superClass);
    }

    @Override
    public List<ClassInfoModel> getSuperClasses() {
        if (superClasses == null) {
            superClasses = getMembers(origin.getSuperclasses(),
                    ClassInfoModel::isNonJDKClass, ClassInfoModel::of);
        }

        return superClasses;
    }

    @Override
    public boolean isAbstract() {
        return origin.isAbstract();
    }

    @Override
    public boolean isAnnotation() {
        return origin.isAnnotation();
    }

    @Override
    public boolean isArrayClass() {
        return origin.isArrayClass();
    }

    @Override
    public boolean isBoolean() {
        return ClassInfoModel.isAssignableFrom(Boolean.class, origin);
    }

    @Override
    public boolean isByte() {
        return ClassInfoModel.isAssignableFrom(Byte.class, origin);
    }

    @Override
    public boolean isCharacter() {
        return ClassInfoModel.isAssignableFrom(Character.class, origin);
    }

    @Override
    public boolean isDate() {
        return ClassInfoModelUtils.isDateAssignable(origin);
    }

    @Override
    public boolean isDateTime() {
        return ClassInfoModelUtils.isDateTimeAssignable(origin);
    }

    @Override
    public boolean isDouble() {
        return ClassInfoModel.isAssignableFrom(Double.class, origin);
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
    public boolean isFloat() {
        return ClassInfoModel.isAssignableFrom(Float.class, origin);
    }

    @Override
    public boolean isInteger() {
        return ClassInfoModel.isAssignableFrom(Integer.class, origin);
    }

    @Override
    public boolean isInterface() {
        return origin.isInterface();
    }

    @Override
    public boolean isInterfaceOrAnnotation() {
        return origin.isInterfaceOrAnnotation();
    }

    @Override
    public boolean isIterable() {
        return ClassInfoModel.isAssignableFrom(Iterable.class, origin);
    }

    @Override
    public boolean isJDKClass() {
        return ClassInfoModel.isJDKClass(origin);
    }

    @Override
    public boolean isLong() {
        return ClassInfoModel.isAssignableFrom(Long.class, origin);
    }

    @Override
    public boolean isMap() {
        return ClassInfoModel.isAssignableFrom(Map.class, origin);
    }

    @Override
    public boolean isNativeObject() {
        return ClassInfoModel.is(origin, Object.class);
    }

    @Override
    public boolean isOptional() {
        return ClassInfoModel.isAssignableFrom(Optional.class, origin);
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
    public boolean isShort() {
        return ClassInfoModel.isAssignableFrom(Short.class, origin);
    }

    @Override
    public boolean isStandardClass() {
        return origin.isStandardClass();
    }

    @Override
    public boolean isStatic() {
        return origin.isStatic();
    }

    @Override
    public boolean isString() {
        return ClassInfoModel.isAssignableFrom(String.class, origin);
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getAnnotationInfo().stream();
    }
}
