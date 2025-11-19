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
package com.vaadin.hilla.parser.core.basic;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.Model;
import com.vaadin.hilla.parser.models.NamedModel;

final class FinalizePlugin extends AbstractPlugin<PluginConfiguration> {
    private final List<String> footsteps = new ArrayList<>();
    private final List<String> members = new ArrayList<>();

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode().getSource() instanceof Model
                && nodePath.getNode().getSource() instanceof NamedModel
                && nodePath.getParentPath().getNode()
                        .getSource() instanceof ClassInfoModel) {
            var model = nodePath.getNode().getSource();
            members.add(String.format("%s %s",
                    ((Model) model).getCommonModelClass().getSimpleName(),
                    ((NamedModel) model).getName()));
        }
        footsteps.add(String.format("-> %s", nodePath.toString()));
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        footsteps.add(String.format("<- %s", nodePath.toString()));
        if (nodePath.getNode() instanceof RootNode) {
            var rootNode = (RootNode) nodePath.getNode();
            var openApi = rootNode.getTarget();
            openApi.addExtension(BasicPlugin.STORAGE_KEY,
                    String.join(", ", members));
            openApi.addExtension(BasicPlugin.FOOTSTEPS_STORAGE_KEY,
                    String.join("\n", footsteps));
        }
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }
}
