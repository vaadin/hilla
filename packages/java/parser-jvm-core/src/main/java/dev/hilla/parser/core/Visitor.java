package dev.hilla.parser.core;

import java.util.Deque;

import dev.hilla.parser.models.Model;

public interface Visitor {
    default void enter(Path<?> path) throws Exception {
    }

    default void exit(Path<?> path) throws Exception {
    }

    int getOrder();
}
