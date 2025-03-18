package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.ClassInfoModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode
import io.swagger.v3.oas.models.tags.Tag
import kotlin.reflect.KClass

class KEndpointNode(
    source: ClassInfoModel,
    target: Tag,
    val kClass: KClass<*>
): EndpointNode(
    source,
    target
)
