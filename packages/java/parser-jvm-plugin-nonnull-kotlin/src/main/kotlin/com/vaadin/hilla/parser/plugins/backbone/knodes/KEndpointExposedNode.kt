package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.ClassInfoModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointExposedNode
import kotlin.reflect.KClass

class KEndpointExposedNode(
    source: ClassInfoModel,
    val kClass: KClass<*>
): EndpointExposedNode(
    source,
), KNode
