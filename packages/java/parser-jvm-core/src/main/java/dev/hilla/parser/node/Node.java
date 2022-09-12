package dev.hilla.parser.node;

public interface Node<S, T> {
    S getSource();

    T getTarget();

    void setTarget(T target);
}
