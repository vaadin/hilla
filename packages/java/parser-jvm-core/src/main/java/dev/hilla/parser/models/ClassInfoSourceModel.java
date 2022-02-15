package dev.hilla.parser.models;

import static dev.hilla.parser.models.ModelUtils.isJDKClass;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

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
            annotations = getMembers(ClassInfo::getAnnotationInfo,
                    AnnotationInfoModel::of);
        }

        return annotations;
    }

    @Override
    public Collection<ClassInfoModel> getDependencies() {
        return null;
    }

    @Override
    public Collection<FieldInfoModel> getFields() {
        if (fields == null) {
            fields = getMembers(ClassInfo::getDeclaredFieldInfo,
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
            innerClasses = getMembers(ClassInfo::getInnerClasses,
                    ClassInfoModel::of);
        }

        return innerClasses;
    }

    @Override
    public <ModelMember extends Model> Stream<ClassInfoModel> getMemberDependenciesStream(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Predicate<ModelMember> filter,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        Objects.requireNonNull(selector);
        return selector.apply(this).stream().filter(Objects::nonNull)
                .filter(Objects.requireNonNull(filter))
                .flatMap(Objects.requireNonNull(dependencyExtractor))
                .distinct();
    }

    @Override
    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        Objects.requireNonNull(wrapper);
        return Objects.requireNonNull(selector).apply(origin).stream()
                .filter(Objects::nonNull).filter(Objects.requireNonNull(filter))
                .map(member -> wrapper.apply(member, this));
    }

    @Override
    public Collection<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = getMembers(ClassInfo::getDeclaredMethodInfo,
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
            superClasses = getMembers(ClassInfo::getSuperclasses,
                    (member) -> !isJDKClass(member), ClassInfoModel::of);
        }

        return superClasses;
    }
}
