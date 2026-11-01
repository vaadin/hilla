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
package com.vaadin.hilla.generator.typescript;

import com.vaadin.hilla.generator.model.EndpointModelPlugin;
import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Output;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;

/**
 * Emits the file declaring each of the types the endpoints refer to, once the
 * walk has left the node of the type and its declaration is built.
 *
 * <p>
 * It reads the declaration {@link EndpointModelPlugin} leaves on the node, so
 * it goes in every chain that plugin is in.
 */
public final class EntityDeclarationPlugin
        extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void emit(NodePath<?> nodePath, Output output) {
        if (nodePath.getNode() instanceof EntityNode entity) {
            entity.getTarget().getDeclaration().map(EntityDeclaration::new)
                    .ifPresent(declaration -> output.emit(entity,
                            declaration.path(), declaration));
        }
    }
}
