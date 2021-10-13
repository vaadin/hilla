package com.vaadin.fusion.parser.plugins.backbone;

import java.util.List;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;

import io.swagger.v3.oas.models.OpenAPI;

public class BackbonePlugin implements Plugin {
    @Override
    public void execute(List<RelativeClassInfo> endpoints, List<RelativeClassInfo> entities,
                        SharedStorage storage) {
        OpenAPI model = storage.getOpenAPI();

        new EndpointProcessor(endpoints, model).process();
        new EntityProcessor(entities, model).process();
    }
}
