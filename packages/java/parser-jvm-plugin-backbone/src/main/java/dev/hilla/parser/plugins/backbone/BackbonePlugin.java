package dev.hilla.parser.plugins.backbone;

import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;

public final class BackbonePlugin implements Plugin {
    public static final String ASSOCIATION_MAP = "BackbonePlugin_AssociationMap";
    private ParserConfig config;
    private int order = 0;
    private SharedStorage storage;

    public void execute(List<ClassInfoModel> endpoints) {
        var associationMap = new AssociationMap();
        var context = new Context(storage.getOpenAPI(), associationMap);

        var endpointProcessor = new EndpointProcessor(context);

        for (var endpoint : endpoints) {
            endpointProcessor.process(endpoint);
        }

        var entityProcessor = new EntityProcessor(context);
        var dependencies = context.getDependencies();

        while (!dependencies.isEmpty()) {
            entityProcessor.process(dependencies.poll());
        }

        storage.getPluginStorage().put(ASSOCIATION_MAP, associationMap);
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
    public void setParserConfig(ParserConfig config) {
        this.config = config;
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }
}
