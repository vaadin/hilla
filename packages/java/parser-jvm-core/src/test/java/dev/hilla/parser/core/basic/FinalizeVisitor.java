package dev.hilla.parser.core.basic;

import java.util.ArrayList;
import java.util.List;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.NamedModel;

final class FinalizeVisitor implements Visitor {
    private static final int order = 3;
    private final List<String> members = new ArrayList<>();

    FinalizeVisitor(SharedStorage storage) {
        storage.getPluginStorage().put(BasicPlugin.STORAGE_KEY, members);
    }

    @Override
    public void exit(NodePath path) {
        var model = path.getModel();

        if (model instanceof FieldInfoModel || model instanceof MethodInfoModel
                || model instanceof ClassInfoModel) {
            members.add(model.getCommonModelClass().getSimpleName() + " "
                    + ((NamedModel) model).getName());
        }
    }

    @Override
    public int getOrder() {
        return order;
    }
}
