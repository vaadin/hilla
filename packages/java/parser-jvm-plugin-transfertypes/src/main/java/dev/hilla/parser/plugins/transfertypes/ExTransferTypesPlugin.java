package dev.hilla.parser.plugins.transfertypes;

import javax.annotation.Nonnull;
import java.util.List;

import dev.hilla.parser.core.ExPlugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Walker;
import dev.hilla.parser.models.ClassInfoModel;

@Deprecated
public final class ExTransferTypesPlugin implements ExPlugin {
    private int order = -100;

    @Override
    public void execute(List<ClassInfoModel> endpoints) {
        var visitors = List.of(new TransferTypesVisitor.PageReplacer(0),
                new TransferTypesVisitor.PageableReplacer(1),
                new TransferTypesVisitor.SortOrderReplacer(2),
                new TransferTypesVisitor.SortReplacer(3),
                new TransferTypesVisitor.UUIDReplacer(4),
                new TransferTypesVisitor.FluxReplacer(5),
                new TransferTypesVisitor.SubscriptionReplacer(6));

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
