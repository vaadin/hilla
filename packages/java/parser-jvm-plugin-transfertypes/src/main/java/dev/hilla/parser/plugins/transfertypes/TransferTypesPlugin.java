package dev.hilla.parser.plugins.transfertypes;

import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Walker;
import dev.hilla.parser.models.ClassInfoModel;

public final class TransferTypesPlugin implements Plugin {
    private int order = -100;

    @Override
    public void execute(List<ClassInfoModel> endpoints) {
        var visitors = List.of(new TransferTypesVisitor.PageReplacer(0),
                new TransferTypesVisitor.PageableReplacer(1),
                new TransferTypesVisitor.SortOrderReplacer(2),
                new TransferTypesVisitor.SortReplacer(3),
                new TransferTypesVisitor.UUIDReplacer(4));

        var walker = new Walker(visitors, endpoints);

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

    }
}
