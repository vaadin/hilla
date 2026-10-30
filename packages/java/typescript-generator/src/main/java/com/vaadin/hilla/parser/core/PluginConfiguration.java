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
package com.vaadin.hilla.parser.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @deprecated Internal API, not deprecated for removal. The Java API of
 *             {@code hilla-typescript-generator} is used only by Hilla itself,
 *             and may change or be removed in any release without notice. Do
 *             not use it in application code or libraries. It is marked
 *             deprecated only to make the compiler warn about such use.
 */
@Deprecated
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface PluginConfiguration {
}
