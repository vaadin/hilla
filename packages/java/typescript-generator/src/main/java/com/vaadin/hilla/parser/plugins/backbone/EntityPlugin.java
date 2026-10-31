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
package com.vaadin.hilla.parser.plugins.backbone;

import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode;
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode;
import com.vaadin.hilla.parser.utils.Signatures;

public final class EntityPlugin
        extends AbstractPlugin<BackbonePluginConfiguration> {
    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        if (!(nodeDependencies.getNode() instanceof TypedNode)) {
            return nodeDependencies;
        }

        var typedNode = (TypedNode) nodeDependencies.getNode();
        var ref = typedNode.getType();
        if (!Signatures.isClassRef(ref)) {
            return nodeDependencies;
        }

        // A class extending a value the browser has, such as a Date or a
        // BigDecimal, is sent as that value and so has no declaration of its
        // own
        if (Signatures.isJDKClass(ref) || Signatures.isDate(ref)
                || Signatures.isIterable(ref) || Signatures.isBigDecimal(ref)
                || Signatures.isBigInteger(ref)) {
            return nodeDependencies;
        }

        return nodeDependencies.appendRelatedNodes(Stream
                .of(EntityNode.of(Signatures.getClass(ref).orElseThrow())));
    }

}
