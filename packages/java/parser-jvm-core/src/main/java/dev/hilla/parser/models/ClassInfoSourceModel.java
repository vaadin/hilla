package dev.hilla.parser.models;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;

final class ClassInfoSourceModel extends AbstractAnnotatedSourceModel<ClassInfo>
        implements ClassInfoModel, SourceModel {
    private final ClassInfoModel superClass;
    private List<ClassInfoModel> chain;
    private List<FieldInfoModel> fields;
    private List<ClassInfoModel> innerClasses;
    private List<ClassInfoModel> interfaces;
    private List<MethodInfoModel> methods;

    public ClassInfoSourceModel(ClassInfo origin) {
        super(origin);

        var originSuperClass = origin.getSuperclass();
        superClass = originSuperClass != null
                ? ClassInfoModel.of(originSuperClass)
                : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ClassInfoModel)) {
            return false;
        }

        var other = (ClassInfoModel) obj;

        return origin.getName().equals(other.getName());
    }

    @Override
    public List<FieldInfoModel> getFields() {
        if (fields == null) {
            fields = origin.getDeclaredFieldInfo().stream()
                    .map(FieldInfoModel::of).collect(Collectors.toList());
        }

        return fields;
    }

    @Override
    public List<ClassInfoModel> getInheritanceChain() {
        if (chain == null) {
            chain = Streams
                    .combine(Stream.of(this),
                            origin.getSuperclasses().stream()
                                    .filter(ClassInfoModel::isNonJDKClass)
                                    .map(ClassInfoModel::of))
                    .collect(Collectors.toList());
        }

        return chain;
    }

    @Override
    public List<ClassInfoModel> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = origin.getInnerClasses().stream()
                    .map(ClassInfoModel::of).collect(Collectors.toList());
        }

        return innerClasses;
    }

    @Override
    public List<ClassInfoModel> getInterfaces() {
        if (interfaces == null) {
            interfaces = origin.getInterfaces().stream().map(ClassInfoModel::of)
                    .collect(Collectors.toList());
        }

        return interfaces;
    }

    @Override
    public List<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = origin.getDeclaredMethodInfo().stream()
                    .map(MethodInfoModel::of).collect(Collectors.toList());
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
    public int hashCode() {
        return 3 + origin.hashCode();
    }

    @Override
    public boolean isAbstract() {
        return origin.isAbstract() || origin.isArrayClass();
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
        return origin.isFinal() || origin.isArrayClass();
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
        return (origin.isPublic() && !origin.isProtected())
                || origin.isArrayClass();
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
