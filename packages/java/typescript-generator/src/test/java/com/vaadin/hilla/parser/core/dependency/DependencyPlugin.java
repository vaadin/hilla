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
package com.vaadin.hilla.parser.core.dependency;

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getMethods;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.test.nodes.EndpointNode;
import com.vaadin.hilla.parser.test.nodes.EntityNode;
import com.vaadin.hilla.parser.test.nodes.FieldNode;
import com.vaadin.hilla.parser.test.nodes.MethodNode;
import com.vaadin.hilla.parser.test.nodes.MethodParameterNode;
import com.vaadin.hilla.parser.test.nodes.TypeSignatureNode;
import com.vaadin.hilla.parser.utils.Signatures;

final class DependencyPlugin extends AbstractPlugin<PluginConfiguration> {
    private final List<String> dependencyMembers = new ArrayList<>();
    private final List<String> endpointDependencies = new ArrayList<>();
    private final List<String> entityDependencies = new ArrayList<>();

    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {
        if (nodePath.getNode().getSource() instanceof Member member
                && (nodePath.getParentPath().getNode() instanceof EntityNode)) {
            dependencyMembers.add(member.getName());
        }
        if ((nodePath.getNode() instanceof EntityNode)
                && (nodePath.getNode().getSource() instanceof Class<?> cls)) {
            entityDependencies.add(cls.getName());
        }
        if (nodePath.getNode().getSource() instanceof Member member && (nodePath
                .getParentPath().getNode() instanceof EndpointNode)) {
            endpointDependencies.add(member.getName());
        }
    }

    /**
     * The members of the entities the walk reached, in the order it reached
     * them.
     */
    List<String> getDependencyMembers() {
        return dependencyMembers;
    }

    /**
     * The methods of the endpoints the walk reached, in the order it reached
     * them.
     */
    List<String> getEndpointDependencies() {
        return endpointDependencies;
    }

    /**
     * The entities the walk reached, in the order it reached them.
     */
    List<String> getEntityDependencies() {
        return entityDependencies;
    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof RootNode) {
            var rootNode = (RootNode) node;
            return nodeDependencies.appendChildNodes(
                    rootNode.getSource().stream().map(EndpointNode::of));
        } else if ((node instanceof EndpointNode)) {
            var cls = (Class<?>) node.getSource();
            return nodeDependencies
                    .appendChildNodes(getMethods(cls).map(MethodNode::of));
        } else if (node instanceof MethodNode) {
            var methodNode = (MethodNode) node;
            var resultTypeNode = TypeSignatureNode
                    .of(methodNode.getSource().getAnnotatedReturnType());
            return nodeDependencies.prependChildNodes(Stream.of(resultTypeNode))
                    .appendChildNodes(Arrays
                            .stream(methodNode.getSource().getParameters())
                            .map(param -> MethodParameterNode.of(param,
                                    Optional.ofNullable(param.getName())
                                            .orElse("_unnamed"))));
        } else if ((node instanceof EntityNode)) {
            var cls = (Class<?>) node.getSource();
            return nodeDependencies.appendChildNodes(
                    Arrays.stream(cls.getDeclaredFields()).map(FieldNode::of));
        } else if (node instanceof FieldNode) {
            var fieldNode = (FieldNode) node;
            return nodeDependencies.appendChildNodes(Stream.of(TypeSignatureNode
                    .of(fieldNode.getSource().getAnnotatedType())));
        } else if ((node instanceof TypeSignatureNode signatureNode)
                && Signatures.isClassRef(signatureNode.getSource())
                && !Signatures.isJDKClass(signatureNode.getSource())) {
            var entityCls = Signatures.getClass(signatureNode.getSource())
                    .orElseThrow();
            return nodeDependencies
                    .appendRelatedNodes(Stream.of(EntityNode.of(entityCls)));
        }
        return nodeDependencies;
    }
}
