package dev.hilla.parser.models;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class ClassInfoReflectionModel extends ClassInfoModel
        implements ReflectionModel {
    private final Class<?> origin;

    ClassInfoReflectionModel(Class<?> origin) {
        this.origin = origin;
    }

    @Override
    public Class<?> get() {
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
        return Modifier.isAbstract(origin.getModifiers());
    }

    @Override
    public boolean isAnnotation() {
        return origin.isAnnotation();
    }

    @Override
    public boolean isArrayClass() {
        return origin.isArray();
    }

    @Override
    public boolean isBigDecimal() {
        return ClassInfoModel.isAssignableFrom(BigDecimal.class, origin);
    }

    @Override
    public boolean isBigInteger() {
        return ClassInfoModel.isAssignableFrom(BigInteger.class, origin);
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
        return Modifier.isFinal(origin.getModifiers());
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
        return origin.isInterface() && !origin.isAnnotation();
    }

    @Override
    public boolean isInterfaceOrAnnotation() {
        return this.isInterface() || this.isAnnotation();
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
        return Modifier.isPrivate(origin.getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(origin.getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(origin.getModifiers());
    }

    @Override
    public boolean isShort() {
        return ClassInfoModel.isAssignableFrom(Short.class, origin);
    }

    @Override
    public boolean isStandardClass() {
        return !this.isAnnotation() && !this.isInterface();
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(origin.getModifiers());
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
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected List<FieldInfoModel> prepareFields() {
        return Arrays.stream(origin.getDeclaredFields()).map(FieldInfoModel::of)
                .collect(Collectors.toList());
    }

    @Override
    protected List<ClassInfoModel> prepareInnerClasses() {
        return Arrays.stream(origin.getDeclaredClasses())
                .map(ClassInfoModel::of).sorted(CLASS_ORDER)
                .collect(Collectors.toList());
    }

    @Override
    protected List<ClassRefSignatureModel> prepareInterfaces() {
        return Arrays.stream(origin.getAnnotatedInterfaces())
                .map(ClassRefSignatureModel::of).collect(Collectors.toList());
    }

    @Override
    protected List<MethodInfoModel> prepareMethods() {
        return Arrays.stream(origin.getDeclaredMethods())
                .filter(method -> !method.isSynthetic())
                .map(MethodInfoModel::of).sorted(MethodInfoModel.METHOD_ORDER)
                .collect(Collectors.toList());
    }

    @Override
    protected PackageInfoModel preparePackage() {
        return PackageInfoModel.of(origin.getPackage());
    }

    @Override
    protected ClassRefSignatureModel prepareSuperClass() {
        var superClass = origin.getAnnotatedSuperclass();

        return superClass != null && ClassInfoModel.isNonJDKClass(superClass)
                ? ClassRefSignatureModel.of(superClass)
                : null;
    }

    @Override
    protected List<TypeParameterModel> prepareTypeParameters() {
        return Arrays.stream(origin.getTypeParameters())
                .map(TypeParameterModel::of).collect(Collectors.toList());
    }
}
