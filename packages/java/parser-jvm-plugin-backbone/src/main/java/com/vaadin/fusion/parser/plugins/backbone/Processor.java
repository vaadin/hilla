package com.vaadin.fusion.parser.plugins.backbone;

import javax.annotation.Nonnull;
import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;

import com.vaadin.fusion.parser.core.RelativeClassInfo;

abstract class Processor {
    protected final List<RelativeClassInfo> classes;
    protected final OpenAPI model;

    public Processor(@Nonnull List<RelativeClassInfo> classes,
            @Nonnull OpenAPI model) {
        this.classes = classes;
        this.model = model;
    }

    public abstract void process();
}
