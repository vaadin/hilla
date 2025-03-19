package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.MethodInfoModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode
import io.swagger.v3.oas.models.PathItem
import kotlin.reflect.KFunction

class KMethodNode(
    source: MethodInfoModel,
    target: PathItem,
    val kFunction: KFunction<*>
): MethodNode(
    source,
    target
), KNode
