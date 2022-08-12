package dev.hilla.parser.core.basic;

import java.util.function.Supplier;
import java.util.stream.Stream;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;

final class ReplaceVisitor implements Visitor {
    private static final int shift = 1;

    private final Supplier<Integer> orderProvider;

    ReplaceVisitor(Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
    }

    @Override
    public void enter(NodePath path) throws NoSuchFieldException {
        var model = path.getModel();

        if (model instanceof MethodInfoModel
                && ((MethodInfoModel) model).getName().equals("bar")) {
            path.replace(Stream
                    .of(Sample.class.getDeclaredField("fieldFoo"),
                            Sample.class.getDeclaredField("fieldBar"))
                    .map(FieldInfoModel::of).toArray(Model[]::new));
        }
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + shift;
    }

    static class Sample {
        private final String fieldBar = "bar";
        private final String fieldFoo = "foo";
    }
}
