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

import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.FieldInfoModel;
import com.vaadin.hilla.parser.test.nodes.EndpointNode;
import com.vaadin.hilla.parser.test.nodes.FieldNode;
import com.vaadin.hilla.parser.test.nodes.MethodNode;

final class ReplacePlugin extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {

    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof EndpointNode) {
            return nodeDependencies.processChildNodes(this::removeBarMethod)
                    .appendChildNodes(getReplacementFields());
        } else {
            return nodeDependencies;
        }
    }

    @NonNull
    private Stream<Node<?, ?>> getReplacementFields() {
        try {
            return Stream
                    .of(Sample.class.getDeclaredField("fieldFoo"),
                            Sample.class.getDeclaredField("fieldBar"))
                    .map(FieldInfoModel::of).map(FieldNode::of);
        } catch (NoSuchFieldException e) {
            return Stream.empty();
        }
    }

    @NonNull
    private Stream<Node<?, ?>> removeBarMethod(
            @NonNull Stream<Node<?, ?>> nodes) {
        return nodes.filter(node -> !((node instanceof MethodNode)
                && ((MethodNode) node).getSource().getName().equals("bar")));
    }

    static class Sample {
        private final String fieldBar = "bar";
        private final String fieldFoo = "foo";
    }
}
