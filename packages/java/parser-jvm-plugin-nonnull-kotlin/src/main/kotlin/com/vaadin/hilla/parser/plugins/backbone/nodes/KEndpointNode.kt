package com.vaadin.hilla.parser.plugins.backbone.nodes

import com.vaadin.hilla.parser.models.ClassInfoModel
import io.swagger.v3.oas.models.tags.Tag
import kotlin.reflect.KClass

class KEndpointNode(source: ClassInfoModel,
                    target: Tag,
                    val clazz: KClass<*>)
    : EndpointNode(source, target)
