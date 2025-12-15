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

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.cleanup;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.MethodInfoModel;
import com.vaadin.hilla.parser.test.nodes.EndpointNode;
import com.vaadin.hilla.parser.test.nodes.EntityNode;
import com.vaadin.hilla.parser.test.nodes.FieldNode;
import com.vaadin.hilla.parser.test.nodes.MethodNode;

final class AddPlugin extends AbstractPlugin<PluginConfiguration> {
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
        if (node instanceof RootNode) {
            var rootNode = (RootNode) node;
            var endpoints = rootNode.getSource().stream()
                    .map(ClassInfoModel::of).collect(Collectors.toList());
            return nodeDependencies
                    .appendChildNodes(endpoints.stream().map(EndpointNode::of))
                    .appendRelatedNodes(endpoints.stream()
                            .map(ClassInfoModel::getInnerClasses)
                            .flatMap(Collection::stream).map(EntityNode::of));
        } else if (node instanceof EndpointNode) {
            return nodeDependencies
                    .appendChildNodes(Stream.concat(
                            ((EndpointNode) node).getSource().getFields()
                                    .stream().map(FieldNode::of),
                            cleanup(((EndpointNode) node).getSource()
                                    .getMethods().stream())
                                    .map(MethodNode::of)))
                    .appendRelatedNodes(Stream.of(
                            EntityNode.of(ClassInfoModel.of(Sample.class))));
        }

        if (node instanceof EntityNode && ((EntityNode) node).getSource()
                .getName().equals(Sample.class.getName())) {
            return nodeDependencies
                    .appendChildNodes(Stream
                            .of(getDeclaredMethod(Sample.class, "methodFoo"),
                                    getDeclaredMethod(Sample.class,
                                            "methodBar"))
                            .map(MethodInfoModel::of).map(MethodNode::of));
        }

        return nodeDependencies;
    }

    static class Sample {
        public void methodBar() {
        }

        public void methodFoo() {
        }
    }
}
