package dev.hilla.parser.core.basic;

import java.util.function.Supplier;
import java.util.stream.Stream;

import dev.hilla.parser.core.Command;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;

final class ReplaceVisitor implements Visitor {
    private static final int step = 100;

    private final Supplier<Integer> orderProvider;

    ReplaceVisitor(Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
    }

    @Override
    public Command enter(Model model, Model parent)
            throws NoSuchFieldException {
        if (model instanceof MethodInfoModel
                && ((MethodInfoModel) model).getName().equals("bar")) {
            return Command.REPLACE(Stream
                    .of(Sample.class.getDeclaredField("fieldFoo"),
                            Sample.class.getDeclaredField("fieldBar"))
                    .map(FieldInfoModel::of).toArray(Model[]::new));
        }

        return Command.DO_NOTHING();
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + step;
    }

    static class Sample {
        private final String fieldBar = "bar";
        private final String fieldFoo = "foo";
    }
}
