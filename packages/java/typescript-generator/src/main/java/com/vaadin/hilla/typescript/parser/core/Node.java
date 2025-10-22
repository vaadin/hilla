package com.vaadin.hilla.typescript.parser.core;

public interface Node<S, T> {
    S getSource();

    T getTarget();

    void setTarget(T target);
}
