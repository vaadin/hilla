package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.AnnotationInfoModel
import com.vaadin.hilla.parser.models.SignatureModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType

class KTypeSignatureNode(
    source: SignatureModel,
    target: Schema<*>,
    annotations: List<AnnotationInfoModel>,
    position: Int?,
    val kType: KType
): TypeSignatureNode(
    source,
    target,
    annotations,
    position
), KNode
