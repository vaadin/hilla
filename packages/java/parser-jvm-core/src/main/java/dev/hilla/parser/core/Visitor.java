package dev.hilla.parser.core;

public interface Visitor {
    default void enter(NodePath path) throws Exception {
    }

    default void exit(NodePath path) throws Exception {
    }

    int getOrder();
}
