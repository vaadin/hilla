package dev.hilla.parser.core;

import dev.hilla.parser.models.Model;

public interface Visitor {
    default Command enter(Model model, Model parent) throws Exception {
        return Command.DO_NOTHING();
    }

    default void exit(Model model, Model parent) throws Exception {
    }

    int getOrder();
}
