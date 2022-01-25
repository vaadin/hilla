package com.vaadin.fusion.parser.core;

import static com.vaadin.fusion.parser.core.ParserUtils.isJDKClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;

public final class RelativeClassInfo
        extends AbstractRelative<ClassInfo, RelativeClassInfo> {
    private final InheritanceChain chain;
    private final RelativeClassInfo superClass;
    private List<RelativeAnnotationInfo> annotations;
    private List<RelativeFieldInfo> fields;
    private List<RelativeClassInfo> innerClasses;
    private List<RelativeMethodInfo> methods;
    private List<RelativeClassInfo> superClasses;

    public RelativeClassInfo(@Nonnull ClassInfo origin) {
        this(origin, null);
    }

    public RelativeClassInfo(@Nonnull ClassInfo origin,
            RelativeClassInfo parent) {
        super(origin, parent);

        var originSuperClass = origin.getSuperclass();
        superClass = originSuperClass != null
                ? new RelativeClassInfo(originSuperClass)
                : null;

        // Should be the latest
        chain = new InheritanceChain();
    }

    public List<RelativeAnnotationInfo> getAnnotations() {
        if (annotations == null) {
            annotations = getMembers(ClassInfo::getAnnotationInfo,
                    RelativeAnnotationInfo::new);
        }

        return annotations;
    }

    public Stream<RelativeAnnotationInfo> getAnnotationsStream() {
        return getAnnotations().stream();
    }

    @Override
    public Stream<RelativeClassInfo> getDependenciesStream() {
        return Stream
                .of(getFieldDependenciesStream(), getMethodDependenciesStream(),
                        getInnerClassesStream(), getSuperClassesStream())
                .flatMap(Function.identity()).distinct();
    }

    public List<RelativeClassInfo> getFieldDependencies() {
        return getFieldDependenciesStream().collect(Collectors.toList());
    }

    public Stream<RelativeClassInfo> getFieldDependenciesStream() {
        return getMemberDependenciesStream(RelativeClassInfo::getFields,
                RelativeFieldInfo::getDependenciesStream);
    }

    public List<RelativeFieldInfo> getFields() {
        if (fields == null) {
            fields = getMembers(ClassInfo::getDeclaredFieldInfo,
                    RelativeFieldInfo::new);
        }

        return fields;
    }

    public Stream<RelativeFieldInfo> getFieldsStream() {
        return getFields().stream();
    }

    public InheritanceChain getInheritanceChain() {
        return chain;
    }

    public List<RelativeClassInfo> getInnerClassDependencies() {
        return getInnerClassDependenciesStream().collect(Collectors.toList());
    }

    public Stream<RelativeClassInfo> getInnerClassDependenciesStream() {
        return getMemberDependenciesStream(RelativeClassInfo::getInnerClasses,
                RelativeClassInfo::getDependenciesStream);
    }

    public List<RelativeClassInfo> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = getMembers(ClassInfo::getInnerClasses,
                    RelativeClassInfo::new);
        }

        return innerClasses;
    }

    public Stream<RelativeClassInfo> getInnerClassesStream() {
        return getInnerClasses().stream();
    }

    public <RelativeMember extends Relative<?>> List<RelativeClassInfo> getMemberDependencies(
            @Nonnull Function<RelativeClassInfo, List<RelativeMember>> selector,
            @Nonnull Function<RelativeMember, Stream<RelativeClassInfo>> dependencyExtractor) {
        return getMemberDependencies(selector, this::defaultFilter,
                dependencyExtractor);
    }

    public <RelativeMember extends Relative<?>> List<RelativeClassInfo> getMemberDependencies(
            @Nonnull Function<RelativeClassInfo, List<RelativeMember>> selector,
            @Nonnull Predicate<RelativeMember> filter,
            @Nonnull Function<RelativeMember, Stream<RelativeClassInfo>> dependencyExtractor) {
        return getMemberDependenciesStream(selector, filter,
                dependencyExtractor).collect(Collectors.toList());
    }

    public <RelativeMember extends Relative<?>> Stream<RelativeClassInfo> getMemberDependenciesStream(
            @Nonnull Function<RelativeClassInfo, List<RelativeMember>> selector,
            @Nonnull Function<RelativeMember, Stream<RelativeClassInfo>> dependencyExtractor) {
        return getMemberDependenciesStream(selector, this::defaultFilter,
                dependencyExtractor);
    }

    public <RelativeMember extends Relative<?>> Stream<RelativeClassInfo> getMemberDependenciesStream(
            @Nonnull Function<RelativeClassInfo, List<RelativeMember>> selector,
            @Nonnull Predicate<RelativeMember> filter,
            @Nonnull Function<RelativeMember, Stream<RelativeClassInfo>> dependencyExtractor) {
        Objects.requireNonNull(selector);
        return selector.apply(this).stream().filter(Objects::nonNull)
                .filter(Objects.requireNonNull(filter))
                .flatMap(Objects.requireNonNull(dependencyExtractor))
                .distinct();
    }

    public <Member, RelativeMember extends Relative<?>> List<RelativeMember> getMembers(
            @Nonnull Function<ClassInfo, List<Member>> selector,
            @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
        return getMembers(selector, this::defaultFilter, wrapper);
    }

    public <Member, RelativeMember extends Relative<?>> List<RelativeMember> getMembers(
            @Nonnull Function<ClassInfo, List<Member>> selector,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
        return getMembersStream(selector, filter, wrapper)
                .collect(Collectors.toList());
    }

    public <Member, RelativeMember extends Relative<?>> Stream<RelativeMember> getMembersStream(
            @Nonnull Function<ClassInfo, List<Member>> selector,
            @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
        return getMembersStream(selector, this::defaultFilter, wrapper);
    }

    public <Member, RelativeMember extends Relative<?>> Stream<RelativeMember> getMembersStream(
            @Nonnull Function<ClassInfo, List<Member>> selector,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
        Objects.requireNonNull(wrapper);
        return Objects.requireNonNull(selector).apply(origin).stream()
                .filter(Objects::nonNull).filter(Objects.requireNonNull(filter))
                .map(member -> wrapper.apply(member, RelativeClassInfo.this));
    }

    public List<RelativeClassInfo> getMethodDependencies() {
        return getMethodDependenciesStream().collect(Collectors.toList());
    }

    public Stream<RelativeClassInfo> getMethodDependenciesStream() {
        return getMemberDependenciesStream(RelativeClassInfo::getMethods,
                RelativeMethodInfo::getDependenciesStream);
    }

    public List<RelativeMethodInfo> getMethods() {
        if (methods == null) {
            methods = getMembers(ClassInfo::getDeclaredMethodInfo,
                    RelativeMethodInfo::new);
        }

        return methods;
    }

    public Stream<RelativeMethodInfo> getMethodsStream() {
        return getMethods().stream();
    }

    @Override
    public Optional<RelativeClassInfo> getParent() {
        return Optional.ofNullable(parent);
    }

    public Optional<RelativeClassInfo> getSuperClass() {
        return Optional.ofNullable(superClass);
    }

    public List<RelativeClassInfo> getSuperClasses() {
        if (superClasses == null) {
            superClasses = getMembers(ClassInfo::getSuperclasses,
                    (member) -> !isJDKClass(member), RelativeClassInfo::new);
        }

        return superClasses;
    }

    public Stream<RelativeClassInfo> getSuperClassesStream() {
        return getSuperClasses().stream();
    }

    private <T> boolean defaultFilter(T member) {
        return true;
    }

    public class InheritanceChain {
        private Collection<RelativeClassInfo> chain;

        private InheritanceChain() {
        }

        public Collection<RelativeClassInfo> getClasses() {
            if (chain == null) {
                var superClasses = getSuperClasses();
                chain = new HashSet<>(superClasses.size() + 1);
                chain.add(RelativeClassInfo.this);
                chain.addAll(superClasses);
            }

            return chain;
        }

        public Stream<RelativeClassInfo> getClassesStream() {
            return getClasses().stream();
        }

        public List<RelativeClassInfo> getDependencies() {
            return getDependenciesStream().collect(Collectors.toList());
        }

        public Stream<RelativeClassInfo> getDependenciesStream() {
            return getClasses().stream()
                    .flatMap(RelativeClassInfo::getDependenciesStream)
                    .distinct();
        }

        public List<RelativeClassInfo> getFieldDependencies() {
            return getFieldDependenciesStream().collect(Collectors.toList());
        }

        public Stream<RelativeClassInfo> getFieldDependenciesStream() {
            return getClasses().stream()
                    .flatMap(RelativeClassInfo::getFieldDependenciesStream)
                    .distinct();
        }

        public List<RelativeFieldInfo> getFields() {
            return getFieldsStream().collect(Collectors.toList());
        }

        public Stream<RelativeFieldInfo> getFieldsStream() {
            return getClasses().stream()
                    .flatMap(RelativeClassInfo::getFieldsStream).distinct();
        }

        public List<RelativeClassInfo> getInnerClassDependencies() {
            return getInnerClassDependenciesStream()
                    .collect(Collectors.toList());
        }

        public Stream<RelativeClassInfo> getInnerClassDependenciesStream() {
            return getClasses().stream()
                    .flatMap(RelativeClassInfo::getInnerClassDependenciesStream)
                    .distinct();
        }

        public List<RelativeClassInfo> getInnerClasses() {
            return getInnerClassesStream().collect(Collectors.toList());
        }

        public Stream<RelativeClassInfo> getInnerClassesStream() {
            return getClasses().stream()
                    .flatMap(RelativeClassInfo::getInnerClassesStream)
                    .distinct();
        }

        public <Member, RelativeMember extends Relative<?>> List<RelativeMember> getMembers(
                @Nonnull Function<ClassInfo, List<Member>> selector,
                @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
            return getMembersStream(selector,
                    RelativeClassInfo.this::defaultFilter, wrapper)
                            .collect(Collectors.toList());
        }

        public <Member, RelativeMember extends Relative<?>> List<RelativeMember> getMembers(
                @Nonnull Function<ClassInfo, List<Member>> selector,
                @Nonnull Predicate<Member> filter,
                @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
            return getMembersStream(selector, filter, wrapper)
                    .collect(Collectors.toList());
        }

        public <Member, RelativeMember extends Relative<?>> Stream<RelativeMember> getMembersStream(
                @Nonnull Function<ClassInfo, List<Member>> selector,
                @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
            return getMembersStream(selector,
                    RelativeClassInfo.this::defaultFilter, wrapper);
        }

        public <Member, RelativeMember extends Relative<?>> Stream<RelativeMember> getMembersStream(
                @Nonnull Function<ClassInfo, List<Member>> selector,
                @Nonnull Predicate<Member> filter,
                @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
            Objects.requireNonNull(wrapper);
            Objects.requireNonNull(filter);
            Objects.requireNonNull(selector);

            return getClasses().stream().flatMap(
                    cls -> cls.getMembersStream(selector, filter, wrapper))
                    .distinct();
        }

        public List<RelativeClassInfo> getMethodDependencies() {
            return getMethodDependenciesStream().collect(Collectors.toList());
        }

        public Stream<RelativeClassInfo> getMethodDependenciesStream() {
            return getClasses().stream()
                    .flatMap(RelativeClassInfo::getMethodDependenciesStream)
                    .distinct();
        }

        public List<RelativeMethodInfo> getMethods() {
            return getMethodsStream().collect(Collectors.toList());
        }

        public Stream<RelativeMethodInfo> getMethodsStream() {
            return getClasses().stream()
                    .flatMap(RelativeClassInfo::getMethodsStream).distinct();
        }
    }
}
