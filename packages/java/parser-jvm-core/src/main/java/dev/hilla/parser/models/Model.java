package dev.hilla.parser.models;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Model {
    Object get();

    default Set<ClassInfoModel> getDependencies() {
        return getDependenciesStream().collect(Collectors.toSet());
    }

    Stream<ClassInfoModel> getDependenciesStream();

    default boolean isReflection() {
        return false;
    }

    default boolean isSource() {
        return false;
    }
}
