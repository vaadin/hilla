package dev.hilla.parser.core;

public interface Node<S, T> {
    S getSource();

    T getTarget();

    void setTarget(T target);
}
