package com.vaadin.fusion.parser.core;

import java.util.Optional;
import java.util.stream.Stream;

public interface Relative<T extends Relative<?>> {
    Object get();

    Optional<T> getParent();

    default Stream<RelativeClassInfo> getDependencies() {
        return Stream.empty();
    }
}
