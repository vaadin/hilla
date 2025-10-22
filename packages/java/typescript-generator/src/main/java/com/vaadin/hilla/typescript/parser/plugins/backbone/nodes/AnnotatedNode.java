package com.vaadin.hilla.typescript.parser.plugins.backbone.nodes;

import java.util.List;

import com.vaadin.hilla.typescript.parser.models.AnnotationInfoModel;

public interface AnnotatedNode {
    List<AnnotationInfoModel> getAnnotations();
}
