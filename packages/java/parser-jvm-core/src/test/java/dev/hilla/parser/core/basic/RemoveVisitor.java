package dev.hilla.parser.core.basic;

import java.util.function.Supplier;

import dev.hilla.parser.core.Command;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.Model;

final class RemoveVisitor implements Visitor {
    private static final int step = 200;

    private final Supplier<Integer> orderProvider;

    RemoveVisitor(Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
    }

    @Override
    public Command enter(Model model, Model parent) {
        if (model instanceof ClassInfoModel
                && ((ClassInfoModel) model).getSimpleName().equals("Baz")) {
            return Command.REMOVE();
        }

        return Command.DO_NOTHING();
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + step;
    }
}
