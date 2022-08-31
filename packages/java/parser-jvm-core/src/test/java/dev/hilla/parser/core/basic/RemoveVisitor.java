package dev.hilla.parser.core.basic;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;

final class RemoveVisitor implements Visitor {
    private static final int order = 2;

    @Override
    public void enter(NodePath path) {
        var model = path.getModel();

        if (!(path instanceof NodePath.ClassDeclaration)
                && model instanceof ClassInfoModel
                && ((ClassInfoModel) model).getSimpleName().equals("Baz")) {
            removeNode(path);
            path.skip();
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    private void removeNode(NodePath path) {
        var model = (ClassInfoModel) path.getModel();
        var parentClass = (ClassInfoModel) path.getParent().getModel();

        parentClass.getInnerClasses().remove(model);
    }
}
