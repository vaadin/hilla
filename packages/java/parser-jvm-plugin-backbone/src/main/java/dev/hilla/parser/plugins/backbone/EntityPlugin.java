package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.ScanLocation;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.SignatureModel;

public final class EntityPlugin
    implements Plugin, Plugin.ScanProcessor, Plugin.Scanner {
    static public final String ENTITY_KIND = "entity";
    private int order = 10;
    private SharedStorage storage;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void process(@Nonnull ScanLocation location) {
        if (location.getCurrent().isPresent() &&
            ENTITY_KIND.equals(location.getCurrent().get().getKind())) {
            ClassInfoModel entity =
                (ClassInfoModel) location.getContext().get(0).getModel();
        }
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }

    @Nonnull
    @Override
    public ScanLocation scan(@Nonnull ScanLocation location) {
        Stream<SignatureModel> signatures = Stream.empty();
        if (location.getCurrent().isPresent() &&
            MethodPlugin.METHOD_KIND.equals(
                location.getCurrent().get().getKind())) {
            MethodInfoModel method =
                (MethodInfoModel) location.getCurrent().get().getModel();
//            method.getDependenciesStream();
        }

        return location;
    }
}
