package com.vaadin.hilla.parser.plugins.backbone.nodes

import com.vaadin.hilla.parser.models.MethodParameterInfoModel
import kotlin.reflect.KParameter

class KMethodParameterNode(
    source: MethodParameterInfoModel,
    target: String,
    val parameter: KParameter
): MethodParameterNode(
    source, target
)
