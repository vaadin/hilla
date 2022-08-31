package dev.hilla.parser.core.basic;

import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Walker;
import dev.hilla.parser.models.ClassInfoModel;

public class BasicPlugin implements Plugin {
    public static final String STORAGE_KEY = "BasicPluginResult";
    private int order = 0;
    private SharedStorage storage;

    @Override
    public void execute(List<ClassInfoModel> endpoints) {
        var walker = new Walker(
                List.of(new AddVisitor(), new ReplaceVisitor(),
                        new RemoveVisitor(), new FinalizeVisitor(storage)),
                endpoints);

        walker.traverse();
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
    }

}
