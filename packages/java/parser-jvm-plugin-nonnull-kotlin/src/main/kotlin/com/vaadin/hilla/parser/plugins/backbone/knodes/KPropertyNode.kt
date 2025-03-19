package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode
import kotlin.reflect.KProperty

class KPropertyNode(
    source: JacksonPropertyModel,
    target: String,
    val kProperty: KProperty<*>
): PropertyNode(
    source,
    target
), KNode
