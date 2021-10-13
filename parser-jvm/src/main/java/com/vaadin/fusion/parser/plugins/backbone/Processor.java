package com.vaadin.fusion.parser.plugins.backbone;

import java.util.List;

import com.vaadin.fusion.parser.core.RelativeClassInfo;

import io.swagger.v3.oas.models.OpenAPI;

abstract class Processor {
    protected final List<RelativeClassInfo> classes;
    protected final OpenAPI model;

    public Processor(List<RelativeClassInfo> classes, OpenAPI model) {
        this.classes = classes;
        this.model = model;
    }

    public abstract void process();
}
