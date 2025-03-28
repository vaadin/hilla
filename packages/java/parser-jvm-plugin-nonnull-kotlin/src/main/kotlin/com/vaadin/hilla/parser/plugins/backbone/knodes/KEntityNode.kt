package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.ClassInfoModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode
import io.swagger.v3.oas.models.media.ObjectSchema
import kotlin.reflect.KClass

class KEntityNode(
    source: ClassInfoModel,
    target: ObjectSchema,
    val kClass: KClass<*>
): EntityNode(
    source,
    target
), KNode
