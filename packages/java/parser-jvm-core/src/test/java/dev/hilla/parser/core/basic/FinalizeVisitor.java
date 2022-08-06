package dev.hilla.parser.core.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.NamedModel;

final class FinalizeVisitor implements Visitor {
    private static final int step = 300;
    private final List<String> members = new ArrayList<>();
    private final Supplier<Integer> orderProvider;

    FinalizeVisitor(SharedStorage storage, Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
        storage.getPluginStorage().put(BasicPlugin.STORAGE_KEY, members);
    }

    @Override
    public void exit(Model model, Model parent) {
        if (model instanceof FieldInfoModel || model instanceof MethodInfoModel
                || model instanceof ClassInfoModel) {
            members.add(model.getCommonModelClass().getSimpleName() + " "
                    + ((NamedModel) model).getName());
        }
    }

    @Override
    public int getOrder() {
        return orderProvider.get() + step;
    }
}
