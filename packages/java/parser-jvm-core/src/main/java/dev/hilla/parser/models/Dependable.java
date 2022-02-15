package dev.hilla.parser.models;

import java.util.Collection;
import java.util.stream.Stream;

public interface Dependable {
    Collection<ClassInfoModel> getDependencies();

    default Stream<ClassInfoModel> getDependenciesStream() {
        return getDependencies().stream();
    }
}
