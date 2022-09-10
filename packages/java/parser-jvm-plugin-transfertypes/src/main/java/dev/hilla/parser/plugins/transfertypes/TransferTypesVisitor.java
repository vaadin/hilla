package dev.hilla.parser.plugins.transfertypes;

import java.util.List;
import java.util.UUID;

import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.runtime.transfertypes.EndpointSubscription;
import dev.hilla.runtime.transfertypes.Flux;
import dev.hilla.runtime.transfertypes.Order;
import dev.hilla.runtime.transfertypes.Pageable;
import dev.hilla.runtime.transfertypes.Sort;

abstract class TransferTypesVisitor implements Visitor {
    private final String from;
    private final int order;
    private final Class<?> to;

    TransferTypesVisitor(Class<?> from, Class<?> to, int order) {
        this(from.getName(), to, order);
    }

    TransferTypesVisitor(String from, Class<?> to, int order) {
        this.from = from;
        this.to = to;
        this.order = order;
    }

    @Override
    public void enter(NodePath path) {
        if (path.isSkipped()) {
            return;
        }

        var model = path.getModel();

        if (model instanceof ClassInfoModel
                && ((ClassInfoModel) model).is(from)) {
//            path.replace(ClassInfoModel.of(to));
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    static final class PageReplacer extends TransferTypesVisitor {
        PageReplacer(int order) {
            super("org.springframework.data.domain.Page", List.class, order);
        }
    }

    static final class PageableReplacer extends TransferTypesVisitor {
        PageableReplacer(int order) {
            super("org.springframework.data.domain.Pageable", Pageable.class,
                    order);
        }
    }

    static final class SortOrderReplacer extends TransferTypesVisitor {
        SortOrderReplacer(int order) {
            super("org.springframework.data.domain.Sort$Order", Order.class,
                    order);
        }
    }

    static final class SortReplacer extends TransferTypesVisitor {
        SortReplacer(int order) {
            super("org.springframework.data.domain.Sort", Sort.class, order);
        }
    }

    static final class UUIDReplacer extends TransferTypesVisitor {
        UUIDReplacer(int order) {
            super(UUID.class, String.class, order);
        }
    }

    static final class FluxReplacer extends TransferTypesVisitor {
        FluxReplacer(int order) {
            super("reactor.core.publisher.Flux", Flux.class, order);
        }
    }

    static final class SubscriptionReplacer extends TransferTypesVisitor {
        SubscriptionReplacer(int order) {
            super("dev.hilla.EndpointSubscription", EndpointSubscription.class, order);
        }
    }
}
