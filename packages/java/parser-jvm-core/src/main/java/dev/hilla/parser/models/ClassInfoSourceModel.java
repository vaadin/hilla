package dev.hilla.parser.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.classgraph.ClassInfo;

final class ClassInfoSourceModel extends ClassInfoModel implements SourceModel {
    private final ClassInfo origin;

    ClassInfoSourceModel(ClassInfo origin) {
        this.origin = origin;
    }

    @Override
    public Object get() {
        return origin;
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
    public boolean isBigDecimal() {
        return ClassInfoModel.isAssignableFrom(BigDecimal.class, origin);
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
        return isDateAssignable(origin, ClassInfoModel::isAssignableFrom);
    }

    @Override
    public boolean isDateTime() {
        return isDateTimeAssignable(origin, ClassInfoModel::isAssignableFrom);
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
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotationInfo());
    }

    @Override
    protected List<FieldInfoModel> prepareFields() {
        return origin.getDeclaredFieldInfo().stream().map(FieldInfoModel::of)
                .collect(Collectors.toList());
    }

    @Override
    protected List<ClassInfoModel> prepareInnerClasses() {
        return origin.getInnerClasses().stream().map(ClassInfoModel::of)
                .collect(Collectors.toList());
    }

    @Override
    protected List<ClassRefSignatureModel> prepareInterfaces() {
        return origin.getTypeSignatureOrTypeDescriptor()
                .getSuperinterfaceSignatures().stream()
                .map(ClassRefSignatureModel::of).collect(Collectors.toList());
    }

    @Override
    protected List<MethodInfoModel> prepareMethods() {
        return origin.getDeclaredMethodInfo().stream().map(MethodInfoModel::of)
                .collect(Collectors.toList());
    }

    @Override
    protected PackageInfoModel preparePackage() {
        return PackageInfoModel.of(origin.getPackageInfo());
    }

    @Override
    protected ClassRefSignatureModel prepareSuperClass() {
        var superClass = origin.getTypeSignatureOrTypeDescriptor()
                .getSuperclassSignature();
        return superClass != null ? ClassRefSignatureModel.of(superClass)
                : null;
    }

    @Override
    protected List<TypeParameterModel> prepareTypeParameters() {
        return origin.getTypeSignatureOrTypeDescriptor().getTypeParameters()
                .stream().map(TypeParameterModel::of)
                .collect(Collectors.toList());
    }
}
