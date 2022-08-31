package dev.hilla.parser.core.basic;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

final class ReplaceVisitor implements Visitor {
    private static final int order = 1;

    @Override
    public void enter(NodePath path) throws NoSuchFieldException {
        var model = path.getModel();

        if (model instanceof MethodInfoModel
                && ((MethodInfoModel) model).getName().equals("bar")) {
            replaceNodes((MethodInfoModel) model);
            path.skip();
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    private void replaceNodes(MethodInfoModel method)
            throws NoSuchFieldException {
        var cls = method.getOwner();

        cls.getMethods().remove(method);

        Stream.of(Sample.class.getDeclaredField("fieldFoo"),
                Sample.class.getDeclaredField("fieldBar"))
                .map(FieldInfoModel::of)
                .collect(Collectors.toCollection(cls::getFields));
    }

    static class Sample {
        private final String fieldBar = "bar";
        private final String fieldFoo = "foo";
    }
}
