package com.vaadin.fusion.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;

public class BackbonePlugin implements Plugin {
    @Override
    public void execute(@Nonnull List<RelativeClassInfo> endpoints,
            @Nonnull List<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage) {
        OpenAPI model = storage.getOpenAPI();

        new EndpointProcessor(endpoints, model).process();
        new EntityProcessor(entities, model).process();
    }
}
