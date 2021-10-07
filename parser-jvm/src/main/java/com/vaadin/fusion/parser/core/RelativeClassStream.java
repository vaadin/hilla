package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

public class RelativeClassStream {
    private final Stream<RelativeClassInfo> stream;

    private RelativeClassStream(Stream<RelativeClassInfo> stream) {
        this.stream = stream;
    }

    @SafeVarargs
    public static RelativeClassStream of(
            final Stream<RelativeClassInfo>... streams) {
        return new RelativeClassStream(
                Stream.of(streams).flatMap(Function.identity()));
    }

    public static RelativeClassStream of(final RelativeClassInfo... classes) {
        return new RelativeClassStream(Stream.of(classes));
    }

    public static RelativeClassStream of(final ClassInfo... classes) {
        return new RelativeClassStream(
                Stream.of(classes).map(RelativeClassInfo::new));
    }

    @SafeVarargs
    public static RelativeClassStream ofRaw(
            final Stream<ClassInfo>... streams) {
        return new RelativeClassStream(Stream.of(streams)
                .flatMap(Function.identity()).map(RelativeClassInfo::new));
    }

    public RelativeClassList collect() {
        return stream.collect(Collectors.toCollection(RelativeClassList::new));
    }

    public <R, A> R collect(Collector<RelativeClassInfo, A, R> collector) {
        return stream.collect(collector);
    }

    public Stream<RelativeAnnotationInfo> getAnnotations(
            Predicate<AnnotationInfo> condition) {
        return getClassMemberStream(ClassInfo::getAnnotationInfo, condition,
                RelativeAnnotationInfo::new);
    }

    public Stream<RelativeAnnotationInfo> getAnnotations() {
        return getClassMemberStream(ClassInfo::getAnnotationInfo,
                RelativeAnnotationInfo::new);
    }

    public Stream<RelativeFieldInfo> getFields(Predicate<FieldInfo> condition) {
        return getClassMemberStream(ClassInfo::getFieldInfo, condition,
                RelativeFieldInfo::new);
    }

    public Stream<RelativeFieldInfo> getFields() {
        return getClassMemberStream(ClassInfo::getFieldInfo,
                RelativeFieldInfo::new);
    }

    public RelativeClassStream getInnerClasses(Predicate<ClassInfo> condition) {
        return RelativeClassStream.of(getClassMemberStream(
                ClassInfo::getInnerClasses, condition, RelativeClassInfo::new));
    }

    public RelativeClassStream getInnerClasses() {
        return RelativeClassStream.of(getClassMemberStream(
                ClassInfo::getInnerClasses, RelativeClassInfo::new));
    }

    public Stream<RelativeMethodInfo> getMethods() {
        return getClassMemberStream(ClassInfo::getMethodInfo,
                RelativeMethodInfo::new);
    }

    public Stream<RelativeMethodInfo> getMethods(
            Predicate<MethodInfo> condition) {
        return getClassMemberStream(ClassInfo::getMethodInfo, condition,
                RelativeMethodInfo::new);
    }

    public Stream<RelativeClassInfo> stream() {
        return stream;
    }

    public RelativeClassStream stream(
            Function<Stream<RelativeClassInfo>, Stream<RelativeClassInfo>> map) {
        return new RelativeClassStream(map.apply(stream));
    }

    private <Member, RelativeMember extends Relative> Stream<RelativeMember> getClassMemberStream(
            Function<ClassInfo, List<Member>> classMemberProvider,
            Function<Member, RelativeMember> relativeWrapper) {
        return stream.map(RelativeClassInfo::get)
                .flatMap(cls -> classMemberProvider.apply(cls).stream())
                .map(relativeWrapper);
    }

    private <Member, RelativeMember extends Relative> Stream<RelativeMember> getClassMemberStream(
            Function<ClassInfo, List<Member>> classMemberProvider,
            Predicate<Member> condition,
            Function<Member, RelativeMember> relativeWrapper) {
        return stream.map(RelativeClassInfo::get)
                .flatMap(cls -> classMemberProvider.apply(cls).stream())
                .filter(condition).map(relativeWrapper);
    }
}
