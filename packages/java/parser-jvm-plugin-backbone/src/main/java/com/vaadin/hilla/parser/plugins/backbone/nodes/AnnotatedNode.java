package com.vaadin.hilla.parser.plugins.backbone.nodes;

import java.util.List;

import com.vaadin.hilla.parser.models.AnnotationInfoModel;

public interface AnnotatedNode {
    List<AnnotationInfoModel> getAnnotations();
}
