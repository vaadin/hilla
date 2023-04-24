package dev.hilla.parser.plugins.backbone.nodes;

import java.util.List;

import dev.hilla.parser.models.AnnotationInfoModel;

public interface AnnotatedNode {
    List<AnnotationInfoModel> getAnnotations();
}
