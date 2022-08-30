package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.core.ScanItem;
import dev.hilla.parser.core.ScanLocation;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.utils.Streams;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointPlugin
    implements Plugin, Plugin.ScanProcessor, Plugin.Scanner {
    static public final String ENDPOINT_KIND = "endpoint";
    private int order = 0;
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
            ENDPOINT_KIND.equals(location.getCurrent().get().getKind())) {
            ScanItem item = location.getCurrent().get();
            ClassInfoModel cls = (ClassInfoModel) item.getModel();
            storage.getOpenAPI()
                .addTagsItem(new Tag().name(cls.getSimpleName()));
        }
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }

    @Nonnull
    @Override
    public ScanLocation scan(@Nonnull ScanLocation location) {
        if (location.getCurrent().isEmpty()) {
            final Stream<ScanItem> endpoints = storage.getScanResult()
                .getClassesWithAnnotation(storage.getEndpointAnnotationName())
                .stream().map(ClassInfoModel::of)
                .map(cls -> new ScanItem(cls, ENDPOINT_KIND));
            return new ScanLocation(location.getContext(),
                Streams.combine(location.getNext().stream(), endpoints)
                    .collect(Collectors.toList()));
        }
        return location;
    }

}
