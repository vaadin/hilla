package dev.hilla.parser.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;

abstract class ClassInfoAbstractModel<T> extends AnnotatedAbstractModel<T>
        implements ClassInfoModel {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };

    private final ClassInfoModel superClass;
    private List<ClassInfoModel> chain;
    private List<FieldInfoModel> fields;
    private List<ClassInfoModel> innerClasses;
    private List<ClassInfoModel> interfaces;
    private List<MethodInfoModel> methods;

    ClassInfoAbstractModel(@Nonnull T origin) {
        super(origin);
        superClass = prepareSuperClass();
    }

    static <T> boolean isDateAssignable(T actor,
            BiPredicate<Class<?>, T> predicate) {
        for (var cls : DATE_CLASSES) {
            if (predicate.test(cls, actor)) {
                return true;
            }
        }

        return false;
    }

    static boolean isDateAssignable(Class<?> actor) {
        return isDateAssignable(actor, ClassInfoModel::isAssignableFrom);
    }

    static boolean isDateAssignable(ClassInfo actor) {
        return isDateAssignable(actor, ClassInfoModel::isAssignableFrom);
    }

    static <T> boolean isDateTimeAssignable(T actor,
            BiPredicate<Class<?>, T> predicate) {
        for (var cls : DATE_TIME_CLASSES) {
            if (predicate.test(cls, actor)) {
                return true;
            }
        }

        return false;
    }

    static boolean isDateTimeAssignable(Class<?> actor) {
        return isDateTimeAssignable(actor, ClassInfoModel::isAssignableFrom);
    }

    static boolean isDateTimeAssignable(ClassInfo actor) {
        return isDateTimeAssignable(actor, ClassInfoModel::isAssignableFrom);
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

        return getName().equals(other.getName());
    }

    @Override
    public List<FieldInfoModel> getFields() {
        if (fields == null) {
            fields = prepareFields();
        }

        return fields;
    }

    @Override
    public List<ClassInfoModel> getInheritanceChain() {
        if (chain == null) {
            chain = prepareInheritanceChain();
        }

        return chain;
    }

    @Override
    public List<ClassInfoModel> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = prepareInnerClasses();
        }

        return innerClasses;
    }

    @Override
    public List<ClassInfoModel> getInterfaces() {
        if (interfaces == null) {
            interfaces = prepareInterfaces();
        }

        return interfaces;
    }

    @Override
    public List<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = prepareMethods();
        }

        return methods;
    }

    @Override
    public Optional<ClassInfoModel> getSuperClass() {
        return Optional.ofNullable(superClass);
    }

    @Override
    public int hashCode() {
        return 3 + getName().hashCode();
    }

    protected abstract List<FieldInfoModel> prepareFields();

    protected abstract List<ClassInfoModel> prepareInheritanceChain();

    protected abstract List<ClassInfoModel> prepareInnerClasses();

    protected abstract List<ClassInfoModel> prepareInterfaces();

    protected abstract List<MethodInfoModel> prepareMethods();

    protected abstract ClassInfoModel prepareSuperClass();
}
