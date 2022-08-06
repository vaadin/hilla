package dev.hilla.parser.core.basic;

import java.util.function.Supplier;
import java.util.stream.Stream;

import dev.hilla.parser.core.Command;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;

final class AddVisitor implements Visitor {
    private static final int step = 0;
    private final Supplier<Integer> orderProvider;

    AddVisitor(Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
    }

    @Override
    public Command enter(Model model, Model parent)
            throws NoSuchMethodException {
        if (model instanceof FieldInfoModel
                && ((FieldInfoModel) model).getName().equals("foo")) {
            return Command.ADD(Stream
                    .of(Sample.class.getDeclaredMethod("methodFoo"),
                            Sample.class.getDeclaredMethod("methodBar"))
                    .map(MethodInfoModel::of).toArray(Model[]::new));
        }

        return Command.DO_NOTHING();
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + step;
    }

    static class Sample {
        public void methodBar() {
        }

        public void methodFoo() {
        }
    }
}
