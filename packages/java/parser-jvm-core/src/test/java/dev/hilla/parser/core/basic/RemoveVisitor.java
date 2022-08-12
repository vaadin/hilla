package dev.hilla.parser.core.basic;

import java.util.function.Supplier;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;

final class RemoveVisitor implements Visitor {
    private static final int shift = 2;

    private final Supplier<Integer> orderProvider;

    RemoveVisitor(Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
    }

    @Override
    public void enter(NodePath path) {
        var model = path.getModel();

        if (model instanceof ClassInfoModel
                && ((ClassInfoModel) model).getSimpleName().equals("Baz")) {
            path.remove();
        }
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + shift;
    }
}
