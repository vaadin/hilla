package dev.hilla.parser.models;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;

final class ClassInfoReflectionModel extends AbstractModel<Type>
        implements ClassInfoModel, ReflectionModel {
    private final ClassInfoModelInheritanceChain chain;
    private final ClassInfoModel superClass;
    private Collection<AnnotationInfoModel> annotations;
    private Collection<FieldInfoModel> fields;
    private Collection<ClassInfoModel> innerClasses;
    private Collection<MethodInfoModel> methods;
    private Collection<ClassInfoModel> superClasses;

    public ClassInfoReflectionModel(Type origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public Collection<AnnotationInfoModel> getAnnotations() {
        return null;
    }

    @Override
    public Collection<ClassInfoModel> getDependencies() {
        return null;
    }

    @Override
    public Collection<FieldInfoModel> getFields() {
        return null;
    }

    @Override
    public ClassInfoModelInheritanceChain getInheritanceChain() {
        return null;
    }

    @Override
    public Collection<ClassInfoModel> getInnerClasses() {
        return null;
    }

    @Override
    public <ModelMember extends Model> Stream<ClassInfoModel> getMemberDependenciesStream(@Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector, @Nonnull Predicate<ModelMember> filter, @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        return null;
    }

    @Override
    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(@Nonnull Function<ClassInfo, Collection<Member>> selector, @Nonnull Predicate<Member> filter, @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return null;
    }

    @Override
    public Collection<MethodInfoModel> getMethods() {
        return null;
    }

    @Override
    public Optional<ClassInfoModel> getSuperClass() {
        return Optional.empty();
    }

    @Override
    public Collection<ClassInfoModel> getSuperClasses() {
        return null;
    }
}
