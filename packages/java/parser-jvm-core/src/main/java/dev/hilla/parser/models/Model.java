package dev.hilla.parser.models;

import java.util.Optional;

public interface Model {
    Object get();

    Optional<Model> getParent();
}
