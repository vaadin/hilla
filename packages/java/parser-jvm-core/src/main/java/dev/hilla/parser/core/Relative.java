package dev.hilla.parser.core;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Relative<T extends Relative<?>> {
    Object get();

    default List<RelativeClassInfo> getDependencies() {
        return getDependenciesStream().collect(Collectors.toList());
    }

    default Stream<RelativeClassInfo> getDependenciesStream() {
        return Stream.empty();
    }

    Optional<T> getParent();
}
