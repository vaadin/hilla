package dev.hilla.parser.core.basic;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

final class AddVisitor implements Visitor {
    private static final int order = 0;

    @Override
    public void enter(NodePath path) throws NoSuchMethodException {
        var model = path.getModel();

        if (model instanceof FieldInfoModel
                && ((FieldInfoModel) model).getName().equals("foo")) {
            addNodes((FieldInfoModel) model);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    private void addNodes(FieldInfoModel field) throws NoSuchMethodException {
        var cls = field.getOwner();

        Stream.of(Sample.class.getDeclaredMethod("methodFoo"),
                Sample.class.getDeclaredMethod("methodBar"))
                .map(MethodInfoModel::of)
                .collect(Collectors.toCollection(cls::getMethods));
    }

    static class Sample {
        public void methodBar() {
        }

        public void methodFoo() {
        }
    }
}
