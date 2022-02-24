package dev.hilla.parser.models;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class ClassInfoReflectionModel
        extends AbstractAnnotatedReflectionModel<Class<?>>
        implements ClassInfoModel, ReflectionModel {
    private final ClassInfoModelInheritanceChain chain;
    private final ClassInfoModel superClass;
    private List<FieldInfoModel> fields;
    private List<ClassInfoModel> innerClasses;
    private List<MethodInfoModel> methods;
    private List<ClassInfoModel> superClasses;

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
    public List<FieldInfoModel> getFields() {
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
    public List<ClassInfoModel> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = getMembers(origin.getDeclaredClasses(),
                    ClassInfoModel::of);
        }

        return innerClasses;
    }

    @Override
    public List<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = getMembers(origin.getDeclaredMethods(),
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
            superClasses = new ArrayList<>();

            var cls = origin.getSuperclass();

            while (cls != null && cls != Object.class) {
                superClasses.add(ClassInfoModel.of(cls));
                cls = cls.getSuperclass();
            }
        }

        return superClasses;
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
        return Modifier.isFinal(origin.getModifiers());
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
        return this.isInterface() || this.isAnnotation();
    }

    @Override
    public boolean isIterable() {
        return ClassInfoModelUtils.isAssignableFrom(Iterable.class, origin);
    }

    @Override
    public boolean isJDKClass() {
        return ClassInfoModelUtils.isJDKClass((Type) origin);
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
        return ClassInfoModelUtils.isAssignableFrom(Short.class, origin);
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
        return ClassInfoModelUtils.isAssignableFrom(String.class, origin);
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }
}
