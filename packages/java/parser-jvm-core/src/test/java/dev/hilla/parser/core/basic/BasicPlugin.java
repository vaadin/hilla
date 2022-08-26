package dev.hilla.parser.core.basic;

import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;

public class BasicPlugin implements Plugin {
    public static final String STORAGE_KEY = "BasicPluginResult";
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
        this.storage = storage;
        this.visitors = List.of(new AddVisitor(this::getOrder),
                new ReplaceVisitor(this::getOrder),
                new RemoveVisitor(this::getOrder),
                new FinalizeVisitor(storage, this::getOrder));
    }

}
