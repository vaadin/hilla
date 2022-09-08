package dev.hilla.parser.node;

import io.swagger.v3.oas.models.media.Schema;

public interface Node<S, T> {
    S getSource();
    T getTarget();

    void setTarget(T target);
}
