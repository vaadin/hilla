package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.MethodParameterInfoModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode
import kotlin.reflect.KParameter

class KMethodParameterNode(
    source: MethodParameterInfoModel,
    target: String,
    val kParameter: KParameter
): MethodParameterNode(
    source,
    target
), KNode
