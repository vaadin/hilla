package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Model {
    Object get();

    default Collection<ClassInfoModel> getDependencies() {
        return getDependenciesStream().collect(Collectors.toSet());
    }

    Stream<ClassInfoModel> getDependenciesStream();

    Optional<Model> getParent();

    default boolean isReflection() {
        return false;
    }

    default boolean isSource() {
        return false;
    }
}
