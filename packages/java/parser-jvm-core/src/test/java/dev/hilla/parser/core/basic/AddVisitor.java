package dev.hilla.parser.core.basic;

import java.util.function.Supplier;
import java.util.stream.Stream;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;

final class AddVisitor implements Visitor {
    private static final int shift = 0;
    private final Supplier<Integer> orderProvider;

    AddVisitor(Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
    }

    @Override
    public void enter(NodePath path) throws NoSuchMethodException {
//        var model = path.getModel();
//
//        if (model instanceof FieldInfoModel
//                && ((FieldInfoModel) model).getName().equals("foo")) {
//            path.add(Stream
//                    .of(Sample.class.getDeclaredMethod("methodFoo"),
//                            Sample.class.getDeclaredMethod("methodBar"))
//                    .map(MethodInfoModel::of).toArray(Model[]::new));
//        }
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + shift;
    }

    static class Sample {
        public void methodBar() {
        }

        public void methodFoo() {
        }
    }
}
