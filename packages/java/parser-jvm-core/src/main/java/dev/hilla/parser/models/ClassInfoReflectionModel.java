package dev.hilla.parser.models;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ClassInfoReflectionModel
        extends AbstractAnnotatedReflectionModel<Class<?>>
        implements ClassInfoModel, ReflectionModel {
    private final ClassInfoModel superClass;
    private List<ClassInfoModel> chain;
    private List<FieldInfoModel> fields;
    private List<ClassInfoModel> innerClasses;
    private List<ClassInfoModel> interfaces;
    private List<MethodInfoModel> methods;

    public ClassInfoReflectionModel(Class<?> origin) {
        super(origin);

        var superClass = origin.getSuperclass();

        this.superClass = superClass != null
                && ClassInfoModel.isNonJDKClass(superClass)
                        ? ClassInfoModel.of(superClass)
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
            fields = Arrays.stream(origin.getDeclaredFields())
                    .map(FieldInfoModel::of).collect(Collectors.toList());
        }

        return fields;
    }

    @Override
    public List<ClassInfoModel> getInheritanceChain() {
        if (chain == null) {
            chain = Stream
                    .<Class<?>> iterate(origin,
                            ((Predicate<Class<?>>) Objects::nonNull)
                                    .and(ClassInfoModel::isNonJDKClass),
                            Class::getSuperclass)
                    .distinct().map(ClassInfoModel::of)
                    .collect(Collectors.toList());
        }

        return chain;
    }

    @Override
    public List<ClassInfoModel> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = Arrays.stream(origin.getDeclaredClasses())
                    .map(ClassInfoModel::of).collect(Collectors.toList());
        }

        return innerClasses;
    }

    @Override
    public List<ClassInfoModel> getInterfaces() {
        if (interfaces == null) {
            interfaces = Arrays.stream(origin.getInterfaces())
                    .map(ClassInfoModel::of).collect(Collectors.toList());
        }

        return interfaces;
    }

    @Override
    public List<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = Arrays.stream(origin.getDeclaredMethods())
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
        return 3 + origin.getName().hashCode();
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
}
