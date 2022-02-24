package dev.hilla.parser.models;

import java.util.List;
import java.util.Map;
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
                    (member) -> !ClassInfoModelUtils.isJDKClass(member),
                    ClassInfoModel::of);
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
        return ClassInfoModelUtils.isAssignableFrom(Boolean.class, origin);
    }

    @Override
    public boolean isByte() {
        return ClassInfoModelUtils.isAssignableFrom(Byte.class, origin);
    }

    @Override
    public boolean isCharacter() {
        return ClassInfoModelUtils.isAssignableFrom(Character.class, origin);
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
        return ClassInfoModelUtils.isAssignableFrom(Double.class, origin);
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
        return ClassInfoModelUtils.isAssignableFrom(Float.class, origin);
    }

    @Override
    public boolean isInteger() {
        return ClassInfoModelUtils.isAssignableFrom(Integer.class, origin);
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
        return ClassInfoModelUtils.isAssignableFrom(Iterable.class, origin);
    }

    @Override
    public boolean isJDKClass() {
        return ClassInfoModelUtils.isJDKClass(origin);
    }

    @Override
    public boolean isLong() {
        return ClassInfoModelUtils.isAssignableFrom(Long.class, origin);
    }

    @Override
    public boolean isMap() {
        return ClassInfoModelUtils.isAssignableFrom(Map.class, origin);
    }

    @Override
    public boolean isNativeObject() {
        return ClassInfoModelUtils.is(origin, Object.class);
    }

    @Override
    public boolean isOptional() {
        return ClassInfoModelUtils.isAssignableFrom(Optional.class, origin);
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
        return ClassInfoModelUtils.isAssignableFrom(Short.class, origin);
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
        return ClassInfoModelUtils.isAssignableFrom(String.class, origin);
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
