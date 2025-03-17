package com.vaadin.hilla.parser.plugins.backbone.nodes

import com.vaadin.hilla.parser.models.MethodInfoModel
import io.swagger.v3.oas.models.PathItem
import kotlin.reflect.KFunction

class KMethodNode(source: MethodInfoModel,
                  target: PathItem,
                  val function: KFunction<*>)
    : MethodNode(source, target)
