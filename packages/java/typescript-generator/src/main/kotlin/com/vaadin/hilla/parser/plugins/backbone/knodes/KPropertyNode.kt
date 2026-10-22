/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.plugins.backbone.knodes

import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode
import kotlin.reflect.KProperty

/**
 * **Internal API.** Not part of the public Hilla API: used only by Hilla
 * itself, and may change or be removed in any release without notice or
 * deprecation. Do not use it in application code or libraries.
 */
class KPropertyNode(
    source: JacksonPropertyModel,
    target: String,
    val kProperty: KProperty<*>
): PropertyNode(
    source,
    target
), KNode
