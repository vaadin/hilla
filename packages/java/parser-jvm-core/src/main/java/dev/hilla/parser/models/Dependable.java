package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface Dependable<T, P extends Dependable<?, ?>> {
    T get();

    Collection<ClassInfoModel> getDependencies();

    default Stream<ClassInfoModel> getDependenciesStream() {
        return getDependencies().stream();
    }

    Optional<P> getParent();
}
