package dev.hilla.parser.core.dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.hilla.parser.core.Path;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.NamedModel;

class DependencyVisitor implements Visitor {
    private final List<String> allDependencies = new ArrayList<>();
    private final List<String> allDependencyMembers = new ArrayList<>();
    private final Supplier<Integer> orderProvider;

    DependencyVisitor(SharedStorage storage, Supplier<Integer> orderProvider) {
        this.orderProvider = orderProvider;
        var pluginStorage = storage.getPluginStorage();
        pluginStorage.put(DependencyPlugin.ALL_DEPS_STORAGE_KEY,
                allDependencies);
        pluginStorage.put(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY,
                allDependencyMembers);
    }

    @Override
    public void exit(Path path) {
        var model = path.getModel();
        var parent = path.getParent().getModel();

        if ((model instanceof FieldInfoModel
                || model instanceof MethodInfoModel)
                && !isParentClassEndpoint((ClassInfoModel) parent)) {
            allDependencyMembers.add(((NamedModel) model).getName());
        } else if (model instanceof ClassInfoModel) {
            allDependencies.add(((ClassInfoModel) model).getName());

            if (parent instanceof ClassInfoModel && ((ClassInfoModel) parent)
                    .getInnerClasses().contains(model)) {
                allDependencyMembers.add(((ClassInfoModel) model).getName());
            }
        }
    }

    @Override
    public int getOrder() {
        return orderProvider.get();
    }

    private boolean isParentClassEndpoint(ClassInfoModel parent) {
        return parent.getAnnotationsStream().anyMatch(annotation -> annotation
                .getName().equals(Endpoint.class.getName()));
    }
}
