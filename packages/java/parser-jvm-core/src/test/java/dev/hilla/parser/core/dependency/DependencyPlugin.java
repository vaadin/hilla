package dev.hilla.parser.core.dependency;

import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;

public class DependencyPlugin implements Plugin {
    public static final String ALL_DEPS_STORAGE_KEY = "DependencyPlugin_AllDeps";
    public static final String DEPS_MEMBERS_STORAGE_KEY = "DependencyPlugin_DepsMembers";
    private int order = 0;
    private SharedStorage storage;
    private List<Visitor> visitors;

    @Override
    public void execute(List<ClassInfoModel> endpoints) {

    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.visitors = List.of(new DependencyVisitor(storage, this::getOrder));
    }
}
